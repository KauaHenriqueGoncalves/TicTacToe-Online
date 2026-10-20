package presentation.socket.room;

import com.tic.tac.toe.presentation.socket.room.*;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomManagerTest {
    private RoomManager manager;
    private WebSocket ws;
    private Connection connection;

    @BeforeEach
    void setUp() {
        manager = new RoomManager();
        ws = mock(WebSocket.class);
        connection = Connection.create(ws, UUID.randomUUID());
    }

    // ---------- Room (memoria) ----------
    @Test
    void roomCreateAndUsers() {
        UUID id = UUID.randomUUID();
        Room room = Room.create(id);
        assertEquals(id, room.getId());
        UUID user = UUID.randomUUID();
        room.addUser(user, ws);
        assertSame(ws, room.getUsers().get(user));
        room.removeUser(user);
        assertTrue(room.getUsers().isEmpty());
    }

    @Test
    void roomCreateRequiresId() {
        assertThrows(RuntimeException.class, () -> Room.create(null));
    }

    @SuppressWarnings("MisorderedAssertEqualsArguments") // ordem intencional
    @Test
    void roomEquality() {
        UUID id = UUID.randomUUID();
        assertEquals(Room.create(id), Room.create(id));
        assertEquals(Room.create(id).hashCode(), Room.create(id).hashCode());
        assertNotEquals(Room.create(id), Room.create(UUID.randomUUID()));
        Object nulo = null;
        Object outroTipo = "x";
        assertNotEquals(Room.create(id), nulo);
        assertNotEquals(Room.create(id), outroTipo);
    }

    // ---------- RoomManager ----------
    @Test
    void createGetJoinAndFindByUser() {
        UUID id = UUID.randomUUID();
        Room room = manager.create(id);
        assertSame(room, manager.get(id));

        manager.join(id, connection);
        assertEquals(id, connection.getRoomId());
        assertSame(room, manager.findByUserId(connection.getUserId()));
        assertNull(manager.findByUserId(UUID.randomUUID()));
    }

    @Test
    void getUnknownThrows() {
        assertThrows(RuntimeException.class, () -> manager.get(UUID.randomUUID()));
    }

    @Test
    void joinUnknownRoomDoesNothing() {
        manager.create(UUID.randomUUID());
        manager.join(UUID.randomUUID(), connection);
        assertNull(connection.getRoomId());
    }

    @Test
    void leaveScenarios() {
        UUID id = UUID.randomUUID();
        manager.create(id);
        manager.join(id, connection);

        manager.leave(id, connection);
        assertNull(connection.getRoomId());
        assertTrue(manager.get(id).getUsers().isEmpty());

        // usuario que nao esta na sala e sala inexistente: apenas logam aviso
        assertDoesNotThrow(() -> manager.leave(id, connection));
        assertDoesNotThrow(() -> manager.leave(UUID.randomUUID(), connection));
    }

    @Test
    void broadcastSendsToRoomUsers() {
        UUID id = UUID.randomUUID();
        manager.create(id);
        manager.join(id, connection);
        manager.broadcast(id, "msg");
        verify(ws).send("msg");
        assertThrows(RuntimeException.class, () -> manager.broadcast(UUID.randomUUID(), "msg"));
    }

    @Test
    void removeAndRemoveById() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Room roomA = manager.create(a);
        manager.create(b);

        manager.remove(roomA);
        assertThrows(RuntimeException.class, () -> manager.get(a));
        assertDoesNotThrow(() -> manager.remove(roomA));

        manager.removeById(b);
        assertThrows(RuntimeException.class, () -> manager.get(b));
        assertDoesNotThrow(() -> manager.removeById(b));
    }

    @Test
    void constructorWithCustomSet() {
        Set<Room> set = new HashSet<>();
        RoomManager custom = new RoomManager(set);
        custom.create(UUID.randomUUID());
        assertEquals(1, set.size());
    }
}
