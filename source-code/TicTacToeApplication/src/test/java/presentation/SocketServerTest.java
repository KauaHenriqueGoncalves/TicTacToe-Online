package presentation;

import com.tic.tac.toe.presentation.socket.*;
import com.tic.tac.toe.AppContext;
import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.event.EventRegister;
import com.tic.tac.toe.presentation.socket.room.Room;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testa o SocketServer com o AppContext real (H2 em memoria) chamando diretamente
 * onOpen / onMessage / onClose / onError com WebSockets mockados.
 * Assim o fluxo completo do jogo e exercitado de forma sincrona e deterministica,
 * sem depender de clientes de rede.
 */
class SocketServerTest {
    private static AppContext context;
    private static SocketServer server;

    @BeforeAll
    static void setUp() throws Exception {
        context = AppContext.getInstance();
        assertSame(context, AppContext.getInstance());
        // inicializa os campos estaticos do servidor (e sobe a porta SOCKET_PORT em background)
        SocketServer.start(context);

        Constructor<SocketServer> constructor = SocketServer.class.getDeclaredConstructor(int.class);
        constructor.setAccessible(true);
        server = constructor.newInstance(0);
    }

    // ---------- helpers ----------
    private static User newUser(String name) {
        String email = "ws-" + name + "-" + UUID.randomUUID() + "@mail.com";
        return context.userRepository.save(User.create(new CreateUserRequestDto(name, email, "senha1234")));
    }

    private static ClientHandshake handshake(String resource) {
        ClientHandshake handshake = mock(ClientHandshake.class);
        when(handshake.getResourceDescriptor()).thenReturn(resource);
        return handshake;
    }

    private static WebSocket open(User user) {
        WebSocket ws = mock(WebSocket.class);
        when(ws.isOpen()).thenReturn(true);
        String token = context.jwtService.generate(user.getId().toString(), 60000);
        server.onOpen(ws, handshake("/?accessToken=" + token));
        return ws;
    }

    private static void send(WebSocket ws, String event, String content) {
        server.onMessage(ws, "{\"event\":\"" + event + "\",\"content\":" + content + "}");
    }

    private static List<String> received(WebSocket ws) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ws, atLeast(0)).send(captor.capture());
        return captor.getAllValues();
    }

    private static boolean receivedContaining(WebSocket ws, String text) {
        for (String message : received(ws)) {
            if (message.contains(text)) return true;
        }
        return false;
    }

    /** Falha mostrando a mensagem caso o servidor tenha devolvido algum erro ao cliente. */
    private static void assertNoErrors(WebSocket ws) {
        for (String message : received(ws)) {
            assertFalse(message.contains("\"event\":\"ERROR\""), "servidor respondeu erro: " + message);
        }
    }

    private static Connection connectionOf(WebSocket ws) {
        return context.connectionManager.getByConnection(ws);
    }

    // ---------- onOpen ----------
    @Test
    void openWithValidTokenRegistersConnection() {
        User user = newUser("ana");
        WebSocket ws = open(user);
        Connection connection = connectionOf(ws);
        assertNotNull(connection);
        assertEquals(user.getId(), connection.getUserId());
        assertNull(connection.getRoomId());
        verify(ws, never()).close(anyInt(), anyString());
    }

    @Test
    void reconnectReplacesOldConnection() {
        User user = newUser("bia");
        WebSocket first = open(user);
        WebSocket second = open(user);
        assertNull(connectionOf(first));
        Connection current = context.connectionManager.getByUserId(user.getId());
        assertNotNull(current);
        assertSame(second, current.getConnection());
    }

    @Test
    void reconnectWhileInRoomRestoresRoom() {
        User user = newUser("caio");
        UUID roomId = UUID.randomUUID();
        Room room = context.roomManager.create(roomId);
        room.addUser(user.getId(), mock(WebSocket.class));

        WebSocket ws = open(user);

        assertEquals(roomId, connectionOf(ws).getRoomId());
        assertSame(ws, room.getUsers().get(user.getId()));
        context.roomManager.removeById(roomId);
    }

    @Test
    void openWithoutTokenIsClosed() {
        WebSocket ws = mock(WebSocket.class);
        server.onOpen(ws, handshake("/"));
        verify(ws).close(1008, "Error connection");
        assertTrue(receivedContaining(ws, "UNAUTHORIZED"));
    }

    @Test
    void openWithInvalidTokenIsClosed() {
        WebSocket ws = mock(WebSocket.class);
        server.onOpen(ws, handshake("/?accessToken=token.invalido.aqui"));
        verify(ws).close(eq(1008), anyString());
        assertNull(connectionOf(ws));
    }

    // ---------- onMessage ----------
    @Test
    void unknownEventReturnsInputInvalid() {
        WebSocket ws = open(newUser("davi"));
        send(ws, "evento.inexistente", "{}");
        assertTrue(receivedContaining(ws, "INPUT_INVALID"));
    }

    @Test
    void malformedJsonDoesNotBreakServer() {
        WebSocket ws = open(newUser("edu"));
        assertDoesNotThrow(() -> server.onMessage(ws, "isso nao e json"));
    }

    @Test
    void globalMessageReachesOtherUsers() {
        WebSocket a = open(newUser("fabi"));
        WebSocket b = open(newUser("gabi"));
        send(a, "global.message", "{\"message\":\"ola lobby\"}");
        assertTrue(receivedContaining(b, "ola lobby"));
        assertNoErrors(a);
    }

    @Test
    void roomsByStatusAnswersRequester() {
        WebSocket ws = open(newUser("hugo"));
        send(ws, "global.rooms.by.status", "{\"status\":\"WAITING\"}");
        assertTrue(receivedContaining(ws, "global.rooms.by.status"));
        assertNoErrors(ws);
    }

    @Test
    void fullGameUntilVictory() {
        User ana = newUser("ana");
        User bia = newUser("bia");
        WebSocket a = open(ana);
        WebSocket b = open(bia);

        // cria a sala
        send(a, "room.create", "{\"name\":\"Sala E2E\"}");
        assertNoErrors(a);
        UUID roomId = connectionOf(a).getRoomId();
        assertNotNull(roomId);
        assertTrue(receivedContaining(a, "Sala criada com sucesso"));

        // segundo jogador entra e conversa
        send(b, "room.join", "{\"roomId\":\"" + roomId + "\"}");
        assertNoErrors(b);
        assertEquals(roomId, connectionOf(b).getRoomId());
        send(a, "room.message", "{\"message\":\"boa sorte\"}");
        assertTrue(receivedContaining(b, "boa sorte"));
        send(b, "room.info", "{}");

        // ambos prontos e a partida comeca
        send(a, "room.ready", "{}");
        send(b, "room.ready", "{}");
        send(a, "room.game.start", "{}");
        assertNoErrors(a);
        assertTrue(receivedContaining(b, "room.game.start"));
        assertEquals(RoomStatus.IN_GAME, context.roomService.findById(roomId).getStatus());

        // X vence pela primeira linha: 0, 1, 2
        Game game = context.gameService.findByRoomId(roomId);
        WebSocket xWs = game.getXPlayerId().equals(ana.getId()) ? a : b;
        WebSocket oWs = xWs == a ? b : a;
        send(xWs, "room.game.play", "{\"position\":0}");
        send(oWs, "room.game.play", "{\"position\":3}");
        send(xWs, "room.game.play", "{\"position\":1}");
        send(oWs, "room.game.play", "{\"position\":4}");
        send(xWs, "room.game.play", "{\"position\":2}");
        assertNoErrors(xWs);
        assertNoErrors(oWs);

        assertTrue(receivedContaining(oWs, "room.game.winner"));
        assertTrue(receivedContaining(xWs, "room.kicked"));
        assertNull(connectionOf(a).getRoomId());
        assertNull(connectionOf(b).getRoomId());
        assertThrows(NotFoundException.class, () -> context.roomService.findById(roomId));
    }

    @Test
    void playOutOfTurnReturnsError() {
        User ana = newUser("ana");
        User bia = newUser("bia");
        WebSocket a = open(ana);
        WebSocket b = open(bia);
        send(a, "room.create", "{\"name\":\"Sala Turno\"}");
        UUID roomId = connectionOf(a).getRoomId();
        send(b, "room.join", "{\"roomId\":\"" + roomId + "\"}");
        send(a, "room.ready", "{}");
        send(b, "room.ready", "{}");
        send(a, "room.game.start", "{}");

        Game game = context.gameService.findByRoomId(roomId);
        WebSocket oWs = game.getOPlayerId().equals(ana.getId()) ? a : b;
        send(oWs, "room.game.play", "{\"position\":0}");
        assertTrue(receivedContaining(oWs, "Not your turn"));
    }

    @Test
    void leaveRoomByMessage() {
        WebSocket a = open(newUser("ivo"));
        WebSocket b = open(newUser("jade"));
        send(a, "room.create", "{\"name\":\"Sala Saida\"}");
        UUID roomId = connectionOf(a).getRoomId();
        send(b, "room.join", "{\"roomId\":\"" + roomId + "\"}");

        send(b, "room.leave", "{}");
        assertNoErrors(b);
        assertNull(connectionOf(b).getRoomId());
        assertNotNull(context.roomService.findById(roomId));

        send(a, "room.leave", "{}");
        assertNoErrors(a);
        assertThrows(NotFoundException.class, () -> context.roomService.findById(roomId));
    }

    // ---------- onClose ----------
    @Test
    void closeUnknownConnectionIsIgnored() {
        assertDoesNotThrow(() -> server.onClose(mock(WebSocket.class), 1000, "bye", true));
    }

    @Test
    void closeLobbyConnectionRemovesIt() {
        WebSocket ws = open(newUser("kai"));
        server.onClose(ws, 1000, "bye", true);
        assertNull(connectionOf(ws));
    }

    @Test
    void closeLastPlayerDeletesRoom() {
        WebSocket ws = open(newUser("lia"));
        send(ws, "room.create", "{\"name\":\"Sala Fechada\"}");
        UUID roomId = connectionOf(ws).getRoomId();

        server.onClose(ws, 1001, "fechou aba", true);

        assertNull(connectionOf(ws));
        assertThrows(NotFoundException.class, () -> context.roomService.findById(roomId));
        assertThrows(RuntimeException.class, () -> context.roomManager.get(roomId));
    }

    @Test
    void closeWithPlayerRemainingKeepsRoom() {
        WebSocket a = open(newUser("max"));
        WebSocket b = open(newUser("nina"));
        send(a, "room.create", "{\"name\":\"Sala Resto\"}");
        UUID roomId = connectionOf(a).getRoomId();
        send(b, "room.join", "{\"roomId\":\"" + roomId + "\"}");

        server.onClose(b, 1001, "caiu", true);

        assertNotNull(context.roomService.findById(roomId));
        assertEquals(1, context.roomManager.get(roomId).getUsers().size());
        assertTrue(receivedContaining(a, "room.info"));
    }

    @Test
    void closeWithRoomThatNoLongerExists() {
        WebSocket ws = open(newUser("otto"));
        connectionOf(ws).setRoomId(UUID.randomUUID());
        assertDoesNotThrow(() -> server.onClose(ws, 1006, "erro", true));
        assertNull(connectionOf(ws));
    }

    // ---------- onError / onStart ----------
    @Test
    void onErrorSendsMessageOnlyWhenOpen() {
        WebSocket open = mock(WebSocket.class);
        when(open.isOpen()).thenReturn(true);
        server.onError(open, new IllegalStateException("falha"));
        assertTrue(receivedContaining(open, "falha"));

        WebSocket closed = mock(WebSocket.class);
        server.onError(closed, new IllegalStateException("falha"));
        verify(closed, never()).send(anyString());

        assertDoesNotThrow(() -> server.onError(null, new IllegalStateException("sem conexao")));
        assertDoesNotThrow(() -> server.onStart());
    }

    // ---------- EventRegister ----------
    @Test
    void eventRegisterBuildsDispatcherWithHandlers() {
        EventDispatcher dispatcher = EventRegister.buildDispatcher(context);
        assertDoesNotThrow(() -> dispatcher.publish(
                new RoomsByStatusEvent(RoomStatus.WAITING, UUID.randomUUID(), true)));
    }
}
