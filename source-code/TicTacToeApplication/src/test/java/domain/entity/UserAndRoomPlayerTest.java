package domain.entity;

import com.tic.tac.toe.domain.entity.*;
import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.infrastructure.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserAndRoomPlayerTest {

    @Test
    void userCreateHashesPassword() {
        User user = User.create(new CreateUserRequestDto(" joao ", " joao@mail.com ", " senha1234 "));
        assertNotNull(user.getId());
        assertEquals("joao", user.getUsername());
        assertEquals("joao@mail.com", user.getEmail());
        assertNotEquals("senha1234", user.getPasswordHash());
        assertTrue(PasswordHasher.matches("senha1234", user.getPasswordHash()));
        assertNotNull(user.getCreatedAt());
    }

    @SuppressWarnings("MisorderedAssertEqualsArguments") // ordem intencional
    @Test
    void userSettersAndEquality() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        User a = new User();
        a.setId(id);
        a.setUsername("u");
        a.setEmail("e@e.com");
        a.setPasswordHash("h");
        a.setCreatedAt(now);
        assertEquals("u", a.getUsername());
        assertEquals("e@e.com", a.getEmail());
        assertEquals("h", a.getPasswordHash());
        assertEquals(now, a.getCreatedAt());

        User b = new User(id, "outro", "x@x.com", "h2", now);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new User(UUID.randomUUID(), "u", "e", "h", now));
        Object nulo = null;
        Object outroTipo = "x";
        assertNotEquals(a, nulo);
        assertNotEquals(a, outroTipo);
    }

    @Test
    void roomPlayerInit() {
        Room room = Room.init("Sala", UUID.randomUUID());
        User user = new User(UUID.randomUUID(), "u", "u@u.com", "h", LocalDateTime.now());
        RoomPlayer rp = RoomPlayer.init(room, user);
        assertNotNull(rp.getId());
        assertSame(room, rp.getRoom());
        assertSame(user, rp.getUser());
        assertFalse(rp.getPlaying());
        assertFalse(rp.getReady());
        assertNotNull(rp.getCreatedAt());
    }

    @Test
    void roomPlayerInitRejectsNulls() {
        Room room = Room.init("Sala", UUID.randomUUID());
        User user = new User();
        assertThrows(RuntimeException.class, () -> RoomPlayer.init(null, user));
        assertThrows(RuntimeException.class, () -> RoomPlayer.init(room, null));
    }

    @SuppressWarnings("MisorderedAssertEqualsArguments") // ordem intencional
    @Test
    void roomPlayerSettersAndEquality() {
        UUID id = UUID.randomUUID();
        Room room = Room.init("Sala", UUID.randomUUID());
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        RoomPlayer rp = new RoomPlayer();
        rp.setId(id);
        rp.setRoom(room);
        rp.setUser(user);
        rp.setPlaying(true);
        rp.setReady(true);
        rp.setCreatedAt(now);
        assertEquals(id, rp.getId());
        assertSame(room, rp.getRoom());
        assertSame(user, rp.getUser());
        assertTrue(rp.getPlaying());
        assertTrue(rp.getReady());
        assertEquals(now, rp.getCreatedAt());

        RoomPlayer same = new RoomPlayer(id, null, null, false, false, now);
        assertEquals(rp, same);
        assertEquals(rp.hashCode(), same.hashCode());
        assertNotEquals(rp, RoomPlayer.init(room, user));
        Object nulo = null;
        Object outroTipo = "x";
        assertNotEquals(rp, nulo);
        assertNotEquals(rp, outroTipo);
    }
}
