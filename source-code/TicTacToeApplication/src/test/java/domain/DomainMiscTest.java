package domain;

import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.*;
import com.tic.tac.toe.domain.exception.*;
import com.tic.tac.toe.domain.validation.EmailValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainMiscTest {

    // ---------- RoomStatus ----------
    @Test
    void roomStatusFromString() {
        assertEquals(RoomStatus.WAITING, RoomStatus.fromString("WAITING"));
        assertEquals(RoomStatus.IN_GAME, RoomStatus.fromString("IN_GAME"));
        assertEquals(RoomStatus.FINISHED, RoomStatus.fromString("FINISHED"));
        assertNull(RoomStatus.fromString("waiting"));
        assertNull(RoomStatus.fromString(null));
    }

    @Test
    void roomStatusContains() {
        for (RoomStatus s : RoomStatus.values()) {
            assertTrue(RoomStatus.constains(s));
            assertEquals(s.name(), s.getStatus());
        }
        assertThrows(NullPointerException.class, () -> RoomStatus.constains(null));
    }

    // ---------- Exceptions ----------
    @Test
    void exceptionsCarryStatusAndError() {
        assertException(new EntityAlreadyExistsException("m"), 409, "ENTITY_ALREADY_EXISTS");
        assertException(new InputInvalidException("m"), 400, "INPUT_INVALID");
        assertException(new NotFoundException("m"), 404, "NOT_FOUND");
        assertException(new RepositoryException("m"), 500, "ERROR_REPOSITORY");
        assertException(new UnauthorizedException("m"), 401, "UNAUTHORIZED");
        assertException(new CustomException(418, "TEAPOT", "m"), 418, "TEAPOT");
    }

    private void assertException(CustomException ex, int status, String error) {
        assertEquals(status, ex.getStatus());
        assertEquals(error, ex.getError());
        assertEquals("m", ex.getMessage());
    }

    // ---------- EmailValidation ----------
    @ParameterizedTest
    @ValueSource(strings = {"joao@mail.com", "a.b+c@dominio.com.br", "user_1@host"})
    void validEmails(String email) {
        assertTrue(EmailValidation.isValid(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "semarroba", "a@", "@dominio.com", "a b@c.com", "a@b@c"})
    void invalidEmails(String email) {
        assertFalse(EmailValidation.isValid(email));
    }

    // ---------- Domain events ----------
    @Test
    void domainEvents() {
        UUID room = UUID.randomUUID();
        UUID user = UUID.randomUUID();

        CreateRoomEvent create = new CreateRoomEvent("Sala", user);
        assertEquals("room.create", create.getEvent());
        assertEquals("Sala", create.getName());
        assertEquals(user, create.getOwnerId());

        GlobalMessageEvent global = new GlobalMessageEvent(user, "oi");
        assertEquals("global.message", global.getEvent());
        assertEquals(user, global.getUserId());
        assertEquals("oi", global.getMessage());
        assertNotNull(global.getSentAt());

        InfoRoomEvent info = new InfoRoomEvent(room, user);
        assertEquals("room.info", info.getEvent());
        assertEquals(room, info.getRoomId());
        assertEquals(user, info.getUserId());

        JoinRoomEvent join = new JoinRoomEvent(room, user);
        assertEquals("room.join", join.getEvent());
        assertEquals(room, join.getRoomId());
        assertEquals(user, join.getUserId());

        LeaveRoomEvent leave = new LeaveRoomEvent(room.toString(), user.toString());
        assertEquals("room.leave", leave.getEvent());
        assertEquals(room.toString(), leave.getRoomId());
        assertEquals(user.toString(), leave.getUserId());

        PlayGameEvent play = new PlayGameEvent(room, user, 4);
        assertEquals("room.game.play", play.getEvent());
        assertEquals(room, play.getRoomId());
        assertEquals(user, play.getUserId());
        assertEquals(4, play.getPosition());

        ReadyRoomEvent ready = new ReadyRoomEvent(room.toString(), user.toString());
        assertEquals("room.ready", ready.getEvent());
        assertEquals(room.toString(), ready.getRoomId());
        assertEquals(user.toString(), ready.getUserId());

        RoomMessageEvent msg = new RoomMessageEvent(user, room, "ola");
        assertEquals("room.message", msg.getEvent());
        assertEquals(user, msg.getUserId());
        assertEquals(room, msg.getRoomId());
        assertEquals("ola", msg.getMessage());

        RoomsByStatusEvent byStatus = new RoomsByStatusEvent(RoomStatus.WAITING, user, true);
        assertEquals("global.rooms.by.status", byStatus.getEvent());
        assertEquals(RoomStatus.WAITING, byStatus.getStatus());
        assertEquals(user, byStatus.getUserId());
        assertTrue(byStatus.isBroadcast());

        StartGameEvent start = new StartGameEvent(room, user);
        assertEquals("room.game.start", start.getEvent());
        assertEquals(room, start.getRoomId());
        assertEquals(user, start.getUserId());
    }
}
