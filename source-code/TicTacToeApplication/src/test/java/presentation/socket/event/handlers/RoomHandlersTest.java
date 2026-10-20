package presentation.socket.event.handlers;

import com.tic.tac.toe.presentation.socket.event.handlers.*;
import com.tic.tac.toe.application.event.EventPublisher;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.event.*;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.service.RoomPlayerService;
import com.tic.tac.toe.domain.service.RoomService;
import com.tic.tac.toe.domain.service.UserService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.util.UUID;

import static presentation.socket.event.handlers.HandlerTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Handlers de dentro da sala: info, mensagem, pronto e sair.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomHandlersTest {
    @Mock
    private RoomService roomService;
    @Mock
    private RoomPlayerService roomPlayerService;
    @Mock
    private UserService userService;
    @Mock
    private EventPublisher publisher;

    private ConnectionManager connections;
    private RoomManager rooms;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        connections = new ConnectionManager();
        rooms = new RoomManager();
        roomId = UUID.randomUUID();
    }

    // ---------- InfoRoomHandler ----------
    @Test
    void infoRoomBroadcastsRoomState() {
        Room room = Room.init("Sala Info", UUID.randomUUID());
        Connection c = connect(connections, UUID.randomUUID());
        joinRoom(rooms, room.getId(), c);
        when(roomService.findById(room.getId())).thenReturn(room);

        new InfoRoomHandler(rooms, roomService, mapper()).handle(new InfoRoomEvent(room.getId(), c.getUserId()));

        assertTrue(anySentContains(c, "room.info"));
        assertTrue(anySentContains(c, "Sala Info"));
    }

    @Test
    void infoRoomNullRoom() {
        when(roomService.findById(roomId)).thenReturn(null);
        InfoRoomHandler handler = new InfoRoomHandler(rooms, roomService, mapper());
        assertThrows(RuntimeException.class, () -> handler.handle(new InfoRoomEvent(roomId, UUID.randomUUID())));
    }

    @Test
    void infoRoomNotInMemoryFails() {
        Room room = Room.init("Sala", UUID.randomUUID());
        when(roomService.findById(room.getId())).thenReturn(room);
        InfoRoomHandler handler = new InfoRoomHandler(rooms, roomService, mapper());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> handler.handle(new InfoRoomEvent(room.getId(), UUID.randomUUID())));
        assertTrue(ex.getMessage().contains("Error on transform"));
    }

    // ---------- MessageRoomHandler ----------
    @Test
    void roomMessageBroadcastsInsideRoom() {
        User ana = user("ana");
        Connection a = connect(connections, ana.getId());
        Connection outside = connect(connections, UUID.randomUUID());
        joinRoom(rooms, roomId, a);
        when(userService.findById(ana.getId())).thenReturn(ana);

        new MessageRoomHandler(connections, rooms, userService, mapper())
                .handle(new RoomMessageEvent(ana.getId(), roomId, "jogada boa"));

        assertTrue(anySentContains(a, "jogada boa"));
        assertTrue(anySentContains(a, "sentAt"));
        assertTrue(sent(outside).isEmpty());
    }

    @Test
    void roomMessageValidations() {
        MessageRoomHandler handler = new MessageRoomHandler(connections, rooms, userService, mapper());
        assertThrows(UnauthorizedException.class,
                () -> handler.handle(new RoomMessageEvent(UUID.randomUUID(), roomId, "x")));

        Connection noRoom = connect(connections, UUID.randomUUID());
        assertThrows(InputInvalidException.class,
                () -> handler.handle(new RoomMessageEvent(noRoom.getUserId(), null, "x")));
    }

    @Test
    void roomMessageRoomMissingInMemory() {
        User ana = user("ana");
        Connection a = connect(connections, ana.getId());
        a.setRoomId(roomId);
        when(userService.findById(ana.getId())).thenReturn(ana);
        MessageRoomHandler handler = new MessageRoomHandler(connections, rooms, userService, mapper());
        assertThrows(RuntimeException.class, () -> handler.handle(new RoomMessageEvent(ana.getId(), roomId, "x")));
    }

    // ---------- ReadyRoomHandler ----------
    @Test
    void readyTogglesAndPublishesInfo() {
        UUID userId = UUID.randomUUID();
        new ReadyRoomHandler(roomPlayerService, publisher)
                .handle(new ReadyRoomEvent(roomId.toString(), userId.toString()));
        verify(roomPlayerService).toggleReady(roomId, userId);
        verify(publisher).publish(any(InfoRoomEvent.class));
    }

    @Test
    void readyPublishFailureIsSwallowed() {
        doThrow(new RuntimeException("x")).when(publisher).publish(any());
        ReadyRoomHandler handler = new ReadyRoomHandler(roomPlayerService, publisher);
        assertDoesNotThrow(() -> handler.handle(new ReadyRoomEvent(roomId.toString(), UUID.randomUUID().toString())));
    }

    @Test
    void readyInvalidIds() {
        ReadyRoomHandler handler = new ReadyRoomHandler(roomPlayerService, publisher);
        assertThrows(InputInvalidException.class, () -> handler.handle(new ReadyRoomEvent("abc", UUID.randomUUID().toString())));
        assertThrows(InputInvalidException.class, () -> handler.handle(new ReadyRoomEvent(roomId.toString(), null)));
        verifyNoInteractions(roomPlayerService);
    }

    // ---------- LeaveRoomHandler ----------
    private LeaveRoomHandler leaveHandler() {
        return new LeaveRoomHandler(connections, rooms, roomService, roomPlayerService, publisher, mapper());
    }

    @Test
    void lastPlayerLeavingDeletesRoom() {
        Connection c = connect(connections, UUID.randomUUID());
        joinRoom(rooms, roomId, c);

        leaveHandler().handle(new LeaveRoomEvent(roomId.toString(), c.getUserId().toString()));

        assertNull(c.getRoomId());
        assertTrue(anySentContains(c, "saiu da sala"));
        verify(roomPlayerService).deleteByRoomIdAndUserId(roomId, c.getUserId());
        verify(roomService).delete(roomId);
        assertThrows(RuntimeException.class, () -> rooms.get(roomId));
        verify(publisher).publish(any(RoomsByStatusEvent.class));
        verify(publisher, never()).publish(any(InfoRoomEvent.class));
    }

    @Test
    void playerLeavingWithOthersKeepsRoom() {
        Connection leaving = connect(connections, UUID.randomUUID());
        Connection staying = connect(connections, UUID.randomUUID());
        joinRoom(rooms, roomId, leaving);
        joinRoom(rooms, roomId, staying);
        doThrow(new NotFoundException("ja removido"))
                .when(roomPlayerService).deleteByRoomIdAndUserId(roomId, leaving.getUserId());

        leaveHandler().handle(new LeaveRoomEvent(roomId.toString(), leaving.getUserId().toString()));

        verify(roomService, never()).delete(any());
        assertEquals(1, rooms.get(roomId).getUsers().size());
        verify(publisher).publish(any(InfoRoomEvent.class));
    }

    @Test
    void leaveToleratesFailures() throws Exception {
        // sala nao existe em memoria, conexao fechada e publisher falhando: nada disso derruba o handler
        Connection c = connect(connections, UUID.randomUUID());
        c.setRoomId(roomId);
        when(c.getConnection().isOpen()).thenReturn(false);
        doThrow(new RuntimeException("x")).when(publisher).publish(any());

        assertDoesNotThrow(() -> leaveHandler()
                .handle(new LeaveRoomEvent(roomId.toString(), c.getUserId().toString())));
        verify(c.getConnection(), never()).send(anyString());

        LeaveRoomHandler broken = new LeaveRoomHandler(connections, rooms, roomService, roomPlayerService, publisher, brokenMapper());
        c.setRoomId(roomId);
        assertDoesNotThrow(() -> broken.handle(new LeaveRoomEvent(roomId.toString(), c.getUserId().toString())));
    }

    @Test
    void leaveValidations() {
        LeaveRoomHandler handler = leaveHandler();
        assertThrows(InputInvalidException.class, () -> handler.handle(new LeaveRoomEvent("x", UUID.randomUUID().toString())));
        assertThrows(UnauthorizedException.class,
                () -> handler.handle(new LeaveRoomEvent(roomId.toString(), UUID.randomUUID().toString())));

        Connection other = connect(connections, UUID.randomUUID());
        other.setRoomId(UUID.randomUUID());
        assertThrows(InputInvalidException.class,
                () -> handler.handle(new LeaveRoomEvent(roomId.toString(), other.getUserId().toString())));
        verifyNoInteractions(roomPlayerService);
    }
}
