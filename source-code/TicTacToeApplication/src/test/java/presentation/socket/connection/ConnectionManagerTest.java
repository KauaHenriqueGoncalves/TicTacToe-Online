package presentation.socket.connection;

import com.tic.tac.toe.presentation.socket.connection.*;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionManagerTest {
    private ConnectionManager manager;
    private WebSocket ws1;
    private WebSocket ws2;
    private Connection c1;
    private Connection c2;

    @BeforeEach
    void setUp() {
        manager = new ConnectionManager();
        ws1 = mock(WebSocket.class);
        ws2 = mock(WebSocket.class);
        c1 = Connection.create(ws1, UUID.randomUUID());
        c2 = Connection.create(ws2, UUID.randomUUID());
    }

    // ---------- Connection ----------
    @Test
    void connectionCreate() {
        assertNotNull(c1.getId());
        assertSame(ws1, c1.getConnection());
        assertNull(c1.getRoomId());
        UUID room = UUID.randomUUID();
        c1.setRoomId(room);
        assertEquals(room, c1.getRoomId());
    }

    @Test
    void connectionCreateValidations() {
        assertThrows(RuntimeException.class, () -> Connection.create(null, UUID.randomUUID()));
        assertThrows(RuntimeException.class, () -> Connection.create(ws1, null));
    }

    @SuppressWarnings("MisorderedAssertEqualsArguments") // ordem intencional
    @Test
    void connectionEqualityById() {
        Object nulo = null;
        Object outroTipo = "x";
        assertNotEquals(c1, c2);
        assertNotEquals(c1, nulo);
        assertNotEquals(c1, outroTipo);
        assertEquals(c1.hashCode(), c1.hashCode());
    }

    // ---------- ConnectionManager ----------
    @Test
    void addAndLookups() {
        manager.add(c1);
        manager.add(c2);
        assertSame(c1, manager.getByUserId(c1.getUserId()));
        assertSame(c2, manager.getByConnection(ws2));
        assertNull(manager.getByUserId(UUID.randomUUID()));
        assertNull(manager.getByConnection(mock(WebSocket.class)));
    }

    @Test
    void addRejectsNull() {
        assertThrows(RuntimeException.class, () -> manager.add(null));
    }

    @Test
    void addValidatesConnectionFields() {
        Connection noId = mock(Connection.class);
        assertThrows(RuntimeException.class, () -> manager.add(noId));

        Connection noSocket = mock(Connection.class);
        when(noSocket.getId()).thenReturn(UUID.randomUUID());
        assertThrows(RuntimeException.class, () -> manager.add(noSocket));

        Connection noUser = mock(Connection.class);
        when(noUser.getId()).thenReturn(UUID.randomUUID());
        when(noUser.getConnection()).thenReturn(ws1);
        assertThrows(RuntimeException.class, () -> manager.add(noUser));
    }

    @Test
    void removeAndRemoveByConnection() {
        manager.add(c1);
        manager.add(c2);
        manager.remove(c1);
        assertNull(manager.getByUserId(c1.getUserId()));
        assertThrows(RuntimeException.class, () -> manager.remove(c1));

        manager.removeByConnection(ws2);
        assertNull(manager.getByConnection(ws2));
        assertThrows(RuntimeException.class, () -> manager.removeByConnection(ws2));
    }

    @Test
    void getAllByRoomId() {
        UUID room = UUID.randomUUID();
        c1.setRoomId(room);
        manager.add(c1);
        manager.add(c2);
        assertEquals(1, manager.getAllByRoomId(room).size());
        assertSame(c1, manager.getAllByRoomId(room).get(0));
        assertTrue(manager.getAllByRoomId(UUID.randomUUID()).isEmpty());
    }

    @Test
    void broadcastSendsToEveryone() {
        manager.add(c1);
        manager.add(c2);
        manager.broadcast("oi");
        verify(ws1).send("oi");
        verify(ws2).send("oi");
    }

    @Test
    void constructorWithCustomSet() {
        Set<Connection> set = new HashSet<>();
        ConnectionManager custom = new ConnectionManager(set);
        custom.add(c1);
        assertTrue(set.contains(c1));
    }
}
