package presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.*;
import com.tic.tac.toe.domain.exception.*;
import com.tic.tac.toe.infrastructure.security.JwtService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.event.SocketEventMapper;
import com.tic.tac.toe.presentation.socket.exception.ExceptionSocketHandler;
import com.tic.tac.toe.presentation.socket.exception.StandardSocketException;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import com.tic.tac.toe.presentation.socket.message.SocketMessageReceive;
import com.tic.tac.toe.presentation.socket.middleware.AuthorizedSocketMiddleware;
import com.tic.tac.toe.presentation.socket.util.SocketUtil;
import io.jsonwebtoken.JwtException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SocketSupportTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private Connection connection;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        connection = Connection.create(mock(WebSocket.class), UUID.randomUUID());
        roomId = UUID.randomUUID();
        connection.setRoomId(roomId);
    }

    private SocketMessageReceive msg(String event, String json) throws Exception {
        return new SocketMessageReceive(event, mapper.readTree(json));
    }

    // ---------- SocketEventMapper ----------
    @Test
    void mapsGlobalEvents() throws Exception {
        GlobalMessageEvent global = (GlobalMessageEvent) SocketEventMapper.toDomainEvent(
                msg("global.message", "{\"message\":\"oi\"}"), connection);
        assertEquals("oi", global.getMessage());
        assertEquals(connection.getUserId(), global.getUserId());

        RoomsByStatusEvent rooms = (RoomsByStatusEvent) SocketEventMapper.toDomainEvent(
                msg("global.rooms.by.status", "{\"status\":\"WAITING\"}"), connection);
        assertEquals(RoomStatus.WAITING, rooms.getStatus());
        assertFalse(rooms.isBroadcast());
    }

    @Test
    void mapsRoomEvents() throws Exception {
        CreateRoomEvent create = (CreateRoomEvent) SocketEventMapper.toDomainEvent(
                msg("room.create", "{\"name\":\"Sala\"}"), connection);
        assertEquals("Sala", create.getName());

        RoomMessageEvent message = (RoomMessageEvent) SocketEventMapper.toDomainEvent(
                msg("room.message", "{\"message\":\"ola\"}"), connection);
        assertEquals(roomId, message.getRoomId());
        assertEquals("ola", message.getMessage());

        UUID target = UUID.randomUUID();
        JoinRoomEvent join = (JoinRoomEvent) SocketEventMapper.toDomainEvent(
                msg("room.join", "{\"roomId\":\"" + target + "\"}"), connection);
        assertEquals(target, join.getRoomId());

        InfoRoomEvent info = (InfoRoomEvent) SocketEventMapper.toDomainEvent(msg("room.info", "{}"), connection);
        assertEquals(roomId, info.getRoomId());

        LeaveRoomEvent leave = (LeaveRoomEvent) SocketEventMapper.toDomainEvent(msg("room.leave", "{}"), connection);
        assertEquals(roomId.toString(), leave.getRoomId());

        ReadyRoomEvent ready = (ReadyRoomEvent) SocketEventMapper.toDomainEvent(msg("room.ready", "{}"), connection);
        assertEquals(connection.getUserId().toString(), ready.getUserId());

        StartGameEvent start = (StartGameEvent) SocketEventMapper.toDomainEvent(msg("room.game.start", "{}"), connection);
        assertEquals(roomId, start.getRoomId());

        PlayGameEvent play = (PlayGameEvent) SocketEventMapper.toDomainEvent(
                msg("room.game.play", "{\"position\":7}"), connection);
        assertEquals(7, play.getPosition());
    }

    @Test
    void unknownEventThrows() {
        InputInvalidException ex = assertThrows(InputInvalidException.class,
                () -> SocketEventMapper.toDomainEvent(msg("nao.existe", "{}"), connection));
        assertTrue(ex.getMessage().contains("nao.existe"));
    }

    // ---------- SocketMessage DTOs ----------
    @Test
    void socketMessageDtos() throws Exception {
        JsonNode node = mapper.readTree("{\"a\":1}");
        SocketMessageReceive receive = new SocketMessageReceive();
        receive.setEvent("e");
        receive.setContent(node);
        assertEquals("e", receive.getEvent());
        assertSame(node, receive.getContent());

        SocketMessageDeliver deliver = new SocketMessageDeliver();
        deliver.setEvent("d");
        deliver.setContent(node);
        assertEquals("d", deliver.getEvent());
        assertSame(node, deliver.getContent());

        SocketMessageReceive parsed = mapper.readValue(
                "{\"event\":\"room.info\",\"content\":{}}", SocketMessageReceive.class);
        assertEquals("room.info", parsed.getEvent());
    }

    // ---------- SocketUtil ----------
    @Test
    void socketUtilExtractToken() throws Exception {
        assertEquals("abc", SocketUtil.extractToken("/?accessToken=abc"));
        assertEquals("a b", SocketUtil.extractToken("/ws?x=1&accessToken=a%20b&y=2"));
        assertNull(SocketUtil.extractToken("/ws"));
        assertNull(SocketUtil.extractToken("/ws?x=1&accessToken"));
        assertNull(SocketUtil.extractToken("/ws?outro=1"));
    }

    // ---------- AuthorizedSocketMiddleware ----------
    @Test
    void socketMiddlewareValidatesToken() throws Exception {
        JwtService jwt = mock(JwtService.class);
        when(jwt.validate("tok")).thenReturn("user-1");
        AuthorizedSocketMiddleware middleware = new AuthorizedSocketMiddleware(jwt);
        ClientHandshake handshake = mock(ClientHandshake.class);
        WebSocket ws = mock(WebSocket.class);

        when(handshake.getResourceDescriptor()).thenReturn("/?accessToken=tok");
        assertEquals("user-1", middleware.authenticate(ws, handshake));

        when(handshake.getResourceDescriptor()).thenReturn("/?accessToken=tok&outro=1");
        assertEquals("user-1", middleware.authenticate(ws, handshake));
    }

    @Test
    void socketMiddlewareRejectsMissingOrInvalidToken() {
        JwtService jwt = mock(JwtService.class);
        when(jwt.validate("ruim")).thenThrow(new JwtException("invalido"));
        AuthorizedSocketMiddleware middleware = new AuthorizedSocketMiddleware(jwt);
        ClientHandshake handshake = mock(ClientHandshake.class);
        WebSocket ws = mock(WebSocket.class);

        when(handshake.getResourceDescriptor()).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> middleware.authenticate(ws, handshake));

        when(handshake.getResourceDescriptor()).thenReturn("/sem-token");
        assertThrows(UnauthorizedException.class, () -> middleware.authenticate(ws, handshake));

        when(handshake.getResourceDescriptor()).thenReturn("/?accessToken=ruim");
        assertThrows(JwtException.class, () -> middleware.authenticate(ws, handshake));
    }

    // ---------- ExceptionSocketHandler ----------
    private String handled(Exception ex) {
        WebSocket ws = mock(WebSocket.class);
        new ExceptionSocketHandler().handle(ws, "room.create", ex);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ws).send(captor.capture());
        return captor.getValue();
    }

    @Test
    void exceptionSocketHandlerMapsEachType() {
        assertTrue(handled(new EntityAlreadyExistsException("m")).contains("ENTITY_ALREADY_EXISTS"));
        assertTrue(handled(new InputInvalidException("m")).contains("INPUT_INVALID"));
        assertTrue(handled(new NotFoundException("m")).contains("NOT_FOUND"));
        assertTrue(handled(new RepositoryException("m")).contains("ERROR_REPOSITORY"));
        assertTrue(handled(new UnauthorizedException("m")).contains("UNAUTHORIZED"));
        String generic = handled(new IllegalStateException("falhou"));
        assertTrue(generic.contains("INTERNET_SERVER_ERROR"));
        assertTrue(generic.contains("falhou"));
        assertTrue(generic.contains("room.create"));
    }

    @Test
    void exceptionSocketHandlerIgnoresCheckedExceptions() {
        WebSocket ws = mock(WebSocket.class);
        new ExceptionSocketHandler().handle(ws, "x", new Exception("checked"));
        verify(ws, never()).send(anyString());
    }

    @Test
    void standardSocketExceptionGetters() {
        Instant now = Instant.now();
        StandardSocketException ex = new StandardSocketException(now, "ERROR", "E", "m", "from");
        assertEquals(now, ex.getTimestamp());
        assertEquals("ERROR", ex.getEvent());
        assertEquals("E", ex.getError());
        assertEquals("m", ex.getMessage());
        assertEquals("from", ex.getFromEvent());
        assertNull(new StandardSocketException().getEvent());
    }
}
