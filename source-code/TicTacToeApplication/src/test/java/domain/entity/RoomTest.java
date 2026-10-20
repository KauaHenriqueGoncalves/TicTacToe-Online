package domain.entity;

import com.tic.tac.toe.domain.entity.*;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void initCreatesWaitingRoom() {
        UUID owner = UUID.randomUUID();
        Room room = Room.init("Sala 1", owner);
        assertNotNull(room.getId());
        assertEquals("Sala 1", room.getName());
        assertEquals(owner, room.getOwnerId());
        assertEquals(RoomStatus.WAITING, room.getStatus());
        assertNull(room.getCurrentPlayerId());
        assertNull(room.getWinnerId());
        assertNull(room.getWinnerName());
        assertNotNull(room.getCreatedAt());
        assertNotNull(room.getPlayers());
        assertTrue(room.getPlayers().isEmpty());
    }

    @Test
    void initRejectsNameAndOwnerNull() {
        assertThrows(RuntimeException.class, () -> Room.init(null, null));
    }

    @Test
    void initAcceptsWhenOnlyOneIsNull() {
        assertNotNull(Room.init(null, UUID.randomUUID()));
        assertNotNull(Room.init("nome", null));
    }

    @Test
    void constructorsAndSetters() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Room simple = new Room(id, "A", RoomStatus.IN_GAME, now);
        assertEquals(id, simple.getId());
        assertEquals(RoomStatus.IN_GAME, simple.getStatus());
        assertEquals(now, simple.getCreatedAt());

        UUID owner = UUID.randomUUID();
        UUID current = UUID.randomUUID();
        UUID winner = UUID.randomUUID();
        Set<RoomPlayer> players = new HashSet<>();
        Room full = new Room(id, "B", owner, RoomStatus.FINISHED, current, winner, "joao", now, null, players);
        assertEquals(owner, full.getOwnerId());
        assertEquals(current, full.getCurrentPlayerId());
        assertEquals(winner, full.getWinnerId());
        assertEquals("joao", full.getWinnerName());
        assertSame(players, full.getPlayers());
        assertNull(full.getGame());

        Room r = new Room();
        Game game = Game.init(r, owner, current);
        r.setId(id);
        r.setName("C");
        r.setOwnerId(owner);
        r.setStatus(RoomStatus.WAITING);
        r.setCurrentPlayerId(current);
        r.setWinnerId(winner);
        r.setWinnerName("x");
        r.setCreatedAt(now);
        r.setGame(game);
        r.setPlayers(Collections.emptySet());
        assertEquals("C", r.getName());
        assertSame(game, r.getGame());
        assertEquals("x", r.getWinnerName());
        assertTrue(r.getPlayers().isEmpty());
    }

    @SuppressWarnings("MisorderedAssertEqualsArguments") // ordem intencional
    @Test
    void equalsAndHashCodeUseId() {
        UUID id = UUID.randomUUID();
        Room a = new Room(id, "A", RoomStatus.WAITING, LocalDateTime.now());
        Room b = new Room(id, "B", RoomStatus.FINISHED, LocalDateTime.now());
        Room c = Room.init("C", UUID.randomUUID());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        Object nulo = null;
        Object outroTipo = "texto";
        assertNotEquals(a, nulo);
        assertNotEquals(a, outroTipo);
    }
}
