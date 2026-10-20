package application.service;

import com.tic.tac.toe.application.service.*;
import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.Mark;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.repositoy.GameRepository;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import com.tic.tac.toe.domain.repositoy.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameServiceImplTest {
    @Mock
    private GameRepository gameRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomPlayerRepository roomPlayerRepository;
    private GameServiceImpl service;

    private User userA;
    private User userB;
    private Room room;

    @BeforeEach
    void setUp() {
        service = new GameServiceImpl(gameRepository, roomRepository, roomPlayerRepository);
        userA = new User(UUID.randomUUID(), "ana", "a@a.com", "h", LocalDateTime.now());
        userB = new User(UUID.randomUUID(), "bia", "b@b.com", "h", LocalDateTime.now());
        room = Room.init("Sala", userA.getId());
    }

    private List<RoomPlayer> players(boolean readyB) {
        RoomPlayer a = RoomPlayer.init(room, userA);
        RoomPlayer b = RoomPlayer.init(room, userB);
        a.setReady(true);
        b.setReady(readyB);
        return new ArrayList<>(Arrays.asList(a, b));
    }

    // ---------- start ----------
    @Test
    void startCreatesGameAndPutsRoomInGame() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(gameRepository.findByRoomId(room.getId())).thenReturn(Optional.empty());
        when(roomPlayerRepository.findByRoomId(room.getId())).thenReturn(players(true));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Game game = service.start(room.getId());

        Set<UUID> ids = new HashSet<>(Arrays.asList(game.getXPlayerId(), game.getOPlayerId()));
        assertEquals(new HashSet<>(Arrays.asList(userA.getId(), userB.getId())), ids);
        assertEquals(RoomStatus.IN_GAME, room.getStatus());
        assertEquals(game.getXPlayerId(), room.getCurrentPlayerId());
        verify(roomRepository).save(room);
    }

    @Test
    void startFailsWhenRoomNotFound() {
        when(roomRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.start(UUID.randomUUID()));
    }

    @Test
    void startFailsWhenGameAlreadyStarted() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(gameRepository.findByRoomId(room.getId()))
                .thenReturn(Optional.of(Game.init(room, userA.getId(), userB.getId())));
        InputInvalidException ex = assertThrows(InputInvalidException.class, () -> service.start(room.getId()));
        assertEquals("Game already started", ex.getMessage());
    }

    @Test
    void startFailsWithoutTwoPlayers() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(gameRepository.findByRoomId(room.getId())).thenReturn(Optional.empty());
        when(roomPlayerRepository.findByRoomId(room.getId()))
                .thenReturn(new ArrayList<>(Collections.singletonList(RoomPlayer.init(room, userA))));
        InputInvalidException ex = assertThrows(InputInvalidException.class, () -> service.start(room.getId()));
        assertEquals("Room needs 2 players", ex.getMessage());
    }

    @Test
    void startFailsWhenSomeoneNotReady() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(gameRepository.findByRoomId(room.getId())).thenReturn(Optional.empty());
        when(roomPlayerRepository.findByRoomId(room.getId())).thenReturn(players(false));
        InputInvalidException ex = assertThrows(InputInvalidException.class, () -> service.start(room.getId()));
        assertEquals("All players must be ready", ex.getMessage());
        verify(gameRepository, never()).save(any());
    }

    // ---------- findByRoomId ----------
    @Test
    void findByRoomIdNotFound() {
        when(gameRepository.findByRoomId(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.findByRoomId(UUID.randomUUID()));
    }

    // ---------- play ----------
    private Game gameInRoom() {
        Game game = Game.init(room, userA.getId(), userB.getId());
        when(gameRepository.findByRoomId(room.getId())).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));
        return game;
    }

    @Test
    void playNormalMoveUpdatesCurrentPlayer() {
        gameInRoom();
        Game result = service.play(room.getId(), userA.getId(), 0);
        assertEquals("X--------", result.getBoard());
        assertEquals(Mark.O, result.getTurn());
        assertEquals(userB.getId(), room.getCurrentPlayerId());

        service.play(room.getId(), userB.getId(), 1);
        assertEquals(userA.getId(), room.getCurrentPlayerId());
    }

    @Test
    void playWinningMoveFinishesRoomWithWinner() {
        Game game = gameInRoom();
        game.setBoard("XX-OO----");
        when(roomPlayerRepository.findByRoomId(room.getId())).thenReturn(players(true));

        service.play(room.getId(), userA.getId(), 2);

        assertEquals(RoomStatus.FINISHED, room.getStatus());
        assertEquals(userA.getId(), room.getWinnerId());
        assertEquals("ana", room.getWinnerName());
    }

    @Test
    void playWinningMoveForO() {
        Game game = gameInRoom();
        game.setBoard("XX-OO-X--");
        game.setTurn(Mark.O);
        when(roomPlayerRepository.findByRoomId(room.getId())).thenReturn(Collections.emptyList());

        service.play(room.getId(), userB.getId(), 5);

        assertEquals(userB.getId(), room.getWinnerId());
        assertNull(room.getWinnerName());
    }

    @Test
    void playDrawFinishesRoom() {
        Game game = gameInRoom();
        game.setBoard("XOXXOOOX-");
        service.play(room.getId(), userA.getId(), 8);
        assertEquals(RoomStatus.FINISHED, room.getStatus());
        assertNull(room.getWinnerId());
    }

    @Test
    void playRejectsWhenGameFinished() {
        Game game = Game.init(room, userA.getId(), userB.getId());
        game.setBoard("XXX------");
        when(gameRepository.findByRoomId(room.getId())).thenReturn(Optional.of(game));
        assertThrows(InputInvalidException.class, () -> service.play(room.getId(), userA.getId(), 5));

        game.setBoard("XOXXOOOXX");
        assertThrows(InputInvalidException.class, () -> service.play(room.getId(), userA.getId(), 5));
        verify(gameRepository, never()).save(any());
    }
}
