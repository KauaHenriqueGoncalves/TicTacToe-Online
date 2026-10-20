package presentation.socket.event.handlers;

import com.tic.tac.toe.presentation.socket.event.handlers.*;
import com.tic.tac.toe.application.dto.request.CreateRoomRequestDto;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.event.*;
import com.tic.tac.toe.domain.exception.InputInvalidException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static presentation.socket.event.handlers.HandlerTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Handlers do lobby: mensagem global, lista de salas, criar sala e entrar na sala.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LobbyHandlersTest {
    @Mock
    private UserService userService;
    @Mock
    private RoomService roomService;
    @Mock
    private RoomPlayerService roomPlayerService;
    @Mock
    private EventDispatcher dispatcher;

    private ConnectionManager connections;
    private RoomManager rooms;

    @BeforeEach
    void setUp() {
        connections = new ConnectionManager();
        rooms = new RoomManager();
    }

    // ---------- GlobalMessageHandler ----------
    @Test
    void globalMessageBroadcastsToEveryone() {
        User ana = user("ana");
        Connection a = connect(connections, ana.getId());
        Connection b = connect(connections, UUID.randomUUID());
        when(userService.findById(ana.getId())).thenReturn(ana);

        new GlobalMessageHandler(connections, userService, mapper())
                .handle(new GlobalMessageEvent(ana.getId(), "bom dia"));

        assertTrue(anySentContains(a, "bom dia"));
        assertTrue(anySentContains(b, "\"from\":\"ana\""));
        assertTrue(anySentContains(b, "global.message"));
    }

    @Test
    void globalMessageSerializationError() throws Exception {
        User ana = user("ana");
        when(userService.findById(ana.getId())).thenReturn(ana);
        GlobalMessageHandler handler = new GlobalMessageHandler(connections, userService, brokenMapper());
        assertThrows(RuntimeException.class, () -> handler.handle(new GlobalMessageEvent(ana.getId(), "x")));
    }

    // ---------- RoomsByStatusHandler ----------
    private RoomsByStatusHandler roomsHandler() {
        Room room = Room.init("Sala", UUID.randomUUID());
        Map<UUID, Long> counts = new HashMap<>();
        counts.put(room.getId(), 1L);
        when(roomService.findAllByStatus("WAITING")).thenReturn(Collections.singletonList(room));
        when(roomPlayerService.countGroupedByRoomId()).thenReturn(counts);
        return new RoomsByStatusHandler(connections, roomPlayerService, roomService, mapper());
    }

    @Test
    void roomsByStatusBroadcast() {
        Connection a = connect(connections, UUID.randomUUID());
        Connection b = connect(connections, UUID.randomUUID());
        roomsHandler().handle(new RoomsByStatusEvent(RoomStatus.WAITING, a.getUserId(), true));
        assertTrue(anySentContains(a, "Sala"));
        assertTrue(anySentContains(b, "global.rooms.by.status"));
    }

    @Test
    void roomsByStatusOnlyToRequester() {
        Connection a = connect(connections, UUID.randomUUID());
        Connection b = connect(connections, UUID.randomUUID());
        roomsHandler().handle(new RoomsByStatusEvent(RoomStatus.WAITING, a.getUserId(), false));
        assertTrue(anySentContains(a, "\"maxPlayers\":2"));
        assertTrue(sent(b).isEmpty());
    }

    @Test
    void roomsByStatusRequesterWithoutConnection() {
        RoomsByStatusHandler handler = roomsHandler();
        assertThrows(RuntimeException.class,
                () -> handler.handle(new RoomsByStatusEvent(RoomStatus.WAITING, UUID.randomUUID(), false)));
    }

    @Test
    void roomsByStatusNullStatus() {
        RoomsByStatusHandler handler = new RoomsByStatusHandler(connections, roomPlayerService, roomService, mapper());
        assertThrows(RuntimeException.class,
                () -> handler.handle(new RoomsByStatusEvent(null, UUID.randomUUID(), true)));
    }

    @Test
    void roomsByStatusSerializationError() throws Exception {
        when(roomService.findAllByStatus("WAITING")).thenReturn(Collections.emptyList());
        when(roomPlayerService.countGroupedByRoomId()).thenReturn(new HashMap<>());
        RoomsByStatusHandler handler = new RoomsByStatusHandler(connections, roomPlayerService, roomService, brokenMapper());
        assertThrows(RuntimeException.class,
                () -> handler.handle(new RoomsByStatusEvent(RoomStatus.WAITING, UUID.randomUUID(), true)));
    }

    // ---------- CreateRoomHandler ----------
    @Test
    void createRoomSuccess() {
        User owner = user("dono");
        Connection c = connect(connections, owner.getId());
        Room room = Room.init("Sala", owner.getId());
        when(roomService.create(any(CreateRoomRequestDto.class))).thenReturn(room);

        new CreateRoomHandler(dispatcher, connections, rooms, roomService, mapper())
                .handle(new CreateRoomEvent("Sala", owner.getId()));

        assertEquals(room.getId(), c.getRoomId());
        assertSame(rooms.get(room.getId()), rooms.findByUserId(owner.getId()));
        assertTrue(anySentContains(c, "Sala criada com sucesso"));
        verify(dispatcher).publish(any(RoomsByStatusEvent.class));
        verify(dispatcher).publish(any(InfoRoomEvent.class));
    }

    @Test
    void createRoomValidations() {
        CreateRoomHandler handler = new CreateRoomHandler(dispatcher, connections, rooms, roomService, mapper());
        assertThrows(UnauthorizedException.class, () -> handler.handle(new CreateRoomEvent("Sala", UUID.randomUUID())));

        Connection c = connect(connections, UUID.randomUUID());
        c.setRoomId(UUID.randomUUID());
        assertThrows(InputInvalidException.class, () -> handler.handle(new CreateRoomEvent("Sala", c.getUserId())));
        verifyNoInteractions(roomService);
    }

    @Test
    void createRoomRollsBackWhenPublishFails() {
        User owner = user("dono");
        Connection c = connect(connections, owner.getId());
        Room room = Room.init("Sala", owner.getId());
        when(roomService.create(any(CreateRoomRequestDto.class))).thenReturn(room);
        doThrow(new RuntimeException("falhou")).when(dispatcher).publish(any());

        CreateRoomHandler handler = new CreateRoomHandler(dispatcher, connections, rooms, roomService, mapper());
        assertThrows(RuntimeException.class, () -> handler.handle(new CreateRoomEvent("Sala", owner.getId())));

        assertNull(c.getRoomId());
        assertThrows(RuntimeException.class, () -> rooms.get(room.getId()));
        verify(roomService).delete(room.getId());
    }

    @Test
    void createRoomRollsBackWhenSerializationFails() throws Exception {
        User owner = user("dono");
        connect(connections, owner.getId());
        Room room = Room.init("Sala", owner.getId());
        when(roomService.create(any(CreateRoomRequestDto.class))).thenReturn(room);

        CreateRoomHandler handler = new CreateRoomHandler(dispatcher, connections, rooms, roomService, brokenMapper());
        assertThrows(RuntimeException.class, () -> handler.handle(new CreateRoomEvent("Sala", owner.getId())));
        verify(roomService).delete(room.getId());
    }

    // ---------- JoinRoomHandler ----------
    private JoinRoomHandler joinHandler() {
        return new JoinRoomHandler(dispatcher, connections, rooms, roomService, userService, roomPlayerService);
    }

    @Test
    void joinRoomSuccess() {
        User owner = user("dono");
        User guest = user("convidado");
        Room room = Room.init("Sala", owner.getId());
        room.getPlayers().add(RoomPlayer.init(room, owner));
        rooms.create(room.getId());
        Connection c = connect(connections, guest.getId());
        when(userService.findById(guest.getId())).thenReturn(guest);
        when(roomService.findById(room.getId())).thenReturn(room);

        joinHandler().handle(new JoinRoomEvent(room.getId(), guest.getId()));

        assertEquals(room.getId(), c.getRoomId());
        verify(roomPlayerService).create(any(RoomPlayer.class));
        verify(dispatcher).publish(any(RoomsByStatusEvent.class));
        verify(dispatcher).publish(any(InfoRoomEvent.class));
    }

    @Test
    void joinRoomValidations() {
        JoinRoomHandler handler = joinHandler();
        assertThrows(RuntimeException.class, () -> handler.handle(new JoinRoomEvent(UUID.randomUUID(), UUID.randomUUID())));

        Connection inRoom = connect(connections, UUID.randomUUID());
        inRoom.setRoomId(UUID.randomUUID());
        assertThrows(RuntimeException.class, () -> handler.handle(new JoinRoomEvent(UUID.randomUUID(), inRoom.getUserId())));
        verifyNoInteractions(roomPlayerService);
    }

    @Test
    void joinRoomFull() {
        User guest = user("convidado");
        Room room = Room.init("Sala", UUID.randomUUID());
        room.getPlayers().add(RoomPlayer.init(room, user("a")));
        room.getPlayers().add(RoomPlayer.init(room, user("b")));
        connect(connections, guest.getId());
        when(userService.findById(guest.getId())).thenReturn(guest);
        when(roomService.findById(room.getId())).thenReturn(room);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> joinHandler().handle(new JoinRoomEvent(room.getId(), guest.getId())));
        assertEquals("Room is full.", ex.getMessage());
        verifyNoInteractions(roomPlayerService);
    }
}
