package presentation.socket.event.handlers;

import com.tic.tac.toe.presentation.socket.event.handlers.*;
import com.tic.tac.toe.application.event.EventPublisher;
import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import com.tic.tac.toe.domain.event.PlayGameEvent;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.event.StartGameEvent;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.service.GameService;
import com.tic.tac.toe.domain.service.RoomService;
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
import javax.persistence.OptimisticLockException;
import java.util.UUID;

import static presentation.socket.event.handlers.HandlerTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Handlers da partida: iniciar e jogar.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameHandlersTest {
    @Mock
    private GameService gameService;
    @Mock
    private RoomService roomService;
    @Mock
    private EventPublisher publisher;

    private ConnectionManager connections;
    private RoomManager rooms;
    private Room room;
    private Connection x;
    private Connection o;
    private Game game;

    @BeforeEach
    void setUp() {
        connections = new ConnectionManager();
        rooms = new RoomManager();
        x = connect(connections, UUID.randomUUID());
        o = connect(connections, UUID.randomUUID());
        room = Room.init("Sala", x.getUserId());
        joinRoom(rooms, room.getId(), x);
        joinRoom(rooms, room.getId(), o);
        game = Game.init(room, x.getUserId(), o.getUserId());
    }

    // ---------- StartGameRoomHandler ----------
    private StartGameRoomHandler startHandler() {
        return new StartGameRoomHandler(connections, rooms, gameService, publisher, mapper());
    }

    @Test
    void startGameBroadcastsBoard() {
        when(gameService.start(room.getId())).thenReturn(game);

        startHandler().handle(new StartGameEvent(room.getId(), x.getUserId()));

        assertTrue(anySentContains(x, "room.game.start"));
        assertTrue(anySentContains(o, "---------"));
        verify(publisher).publish(any(InfoRoomEvent.class));
        verify(publisher).publish(any(RoomsByStatusEvent.class));
    }

    @Test
    void startGamePublishFailureIsSwallowed() {
        when(gameService.start(room.getId())).thenReturn(game);
        doThrow(new RuntimeException("x")).when(publisher).publish(any());
        assertDoesNotThrow(() -> startHandler().handle(new StartGameEvent(room.getId(), x.getUserId())));
    }

    @Test
    void startGameValidations() {
        StartGameRoomHandler handler = startHandler();
        assertThrows(UnauthorizedException.class, () -> handler.handle(new StartGameEvent(room.getId(), UUID.randomUUID())));
        assertThrows(InputInvalidException.class, () -> handler.handle(new StartGameEvent(UUID.randomUUID(), x.getUserId())));
        verifyNoInteractions(gameService);
    }

    @Test
    void startGameSerializationError() throws Exception {
        when(gameService.start(room.getId())).thenReturn(game);
        StartGameRoomHandler handler = new StartGameRoomHandler(connections, rooms, gameService, publisher, brokenMapper());
        assertThrows(RuntimeException.class, () -> handler.handle(new StartGameEvent(room.getId(), x.getUserId())));
    }

    // ---------- PlayGameHandler ----------
    private PlayGameHandler playHandler() {
        return new PlayGameHandler(connections, rooms, gameService, roomService, publisher, mapper());
    }

    @Test
    void normalMoveOnlyPublishesInfo() {
        game.play(x.getUserId(), 0);
        when(gameService.play(room.getId(), x.getUserId(), 0)).thenReturn(game);

        playHandler().handle(new PlayGameEvent(room.getId(), x.getUserId(), 0));

        verify(publisher).publish(any(InfoRoomEvent.class));
        verify(publisher, never()).publish(any(RoomsByStatusEvent.class));
        assertFalse(anySentContains(x, "room.game.winner"));
    }

    @Test
    void winningMoveAnnouncesWinnerAndClosesRoom() {
        game.setBoard("XXX-OO---");
        room.setWinnerName("vencedor");
        when(gameService.play(room.getId(), x.getUserId(), 2)).thenReturn(game);

        playHandler().handle(new PlayGameEvent(room.getId(), x.getUserId(), 2));

        assertTrue(anySentContains(o, "room.game.winner"));
        assertTrue(anySentContains(o, "vencedor"));
        assertTrue(anySentContains(o, x.getUserId().toString()));
        assertTrue(anySentContains(x, "room.kicked"));
        assertNull(x.getRoomId());
        assertNull(o.getRoomId());
        assertThrows(RuntimeException.class, () -> rooms.get(room.getId()));
        verify(roomService).delete(room.getId());
        verify(publisher).publish(any(RoomsByStatusEvent.class));
    }

    @Test
    void winningMoveForOAndDeleteFailureIsSwallowed() {
        game.setBoard("XX-OOOX--");
        when(gameService.play(room.getId(), o.getUserId(), 5)).thenReturn(game);
        doThrow(new RuntimeException("db")).when(roomService).delete(room.getId());
        doThrow(new RuntimeException("pub")).when(publisher).publish(any());

        assertDoesNotThrow(() -> playHandler().handle(new PlayGameEvent(room.getId(), o.getUserId(), 5)));
        assertTrue(anySentContains(x, o.getUserId().toString()));
    }

    @Test
    void drawMoveAnnouncesDraw() {
        game.setBoard("XOXXOOOXX");
        when(gameService.play(room.getId(), x.getUserId(), 8)).thenReturn(game);

        playHandler().handle(new PlayGameEvent(room.getId(), x.getUserId(), 8));

        assertTrue(anySentContains(x, "room.game.draw"));
        assertTrue(anySentContains(o, "Empate"));
        verify(roomService, never()).delete(any());
    }

    @Test
    void concurrentMoveBecomesInputInvalid() {
        when(gameService.play(any(UUID.class), any(UUID.class), anyInt())).thenThrow(new OptimisticLockException());
        PlayGameHandler handler = playHandler();
        InputInvalidException ex = assertThrows(InputInvalidException.class,
                () -> handler.handle(new PlayGameEvent(room.getId(), x.getUserId(), 0)));
        assertEquals("Concurrent move, try again", ex.getMessage());
    }

    @Test
    void playValidations() {
        PlayGameHandler handler = playHandler();
        assertThrows(UnauthorizedException.class, () -> handler.handle(new PlayGameEvent(room.getId(), UUID.randomUUID(), 0)));
        assertThrows(InputInvalidException.class, () -> handler.handle(new PlayGameEvent(UUID.randomUUID(), x.getUserId(), 0)));
        verifyNoInteractions(gameService);
    }

    @Test
    void playSerializationErrorIsLogged() throws Exception {
        game.setBoard("XOXXOOOXX");
        when(gameService.play(room.getId(), x.getUserId(), 8)).thenReturn(game);
        PlayGameHandler handler = new PlayGameHandler(connections, rooms, gameService, roomService, publisher, brokenMapper());
        assertDoesNotThrow(() -> handler.handle(new PlayGameEvent(room.getId(), x.getUserId(), 8)));
    }
}
