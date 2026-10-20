package domain.entity;

import com.tic.tac.toe.domain.entity.*;
import com.tic.tac.toe.domain.entity.enums.Mark;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private UUID x;
    private UUID o;
    private Room room;
    private Game game;

    @BeforeEach
    void setUp() {
        x = UUID.randomUUID();
        o = UUID.randomUUID();
        room = Room.init("Sala", x);
        game = Game.init(room, x, o);
    }

    @Test
    @DisplayName("init cria jogo com tabuleiro vazio e vez do X")
    void initCreatesEmptyBoard() {
        assertNotNull(game.getId());
        assertEquals("---------", game.getBoard());
        assertEquals(Mark.X, game.getTurn());
        assertEquals(x, game.getXPlayerId());
        assertEquals(o, game.getOPlayerId());
        assertSame(room, game.getRoom());
        assertNull(game.winner());
        assertFalse(game.isDraw());
    }

    @Test
    @DisplayName("play marca a posicao e alterna a vez")
    void playMarksAndSwitchesTurn() {
        game.play(x, 4);
        assertEquals("----X----", game.getBoard());
        assertEquals(Mark.O, game.getTurn());

        game.play(o, 0);
        assertEquals("O---X----", game.getBoard());
        assertEquals(Mark.X, game.getTurn());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 9, 100})
    @DisplayName("play rejeita posicao fora de 0..8")
    void playRejectsInvalidPosition(int pos) {
        InputInvalidException ex = assertThrows(InputInvalidException.class, () -> game.play(x, pos));
        assertEquals("Invalid position", ex.getMessage());
    }

    @Test
    @DisplayName("play rejeita jogador fora da vez")
    void playRejectsWrongTurn() {
        InputInvalidException ex = assertThrows(InputInvalidException.class, () -> game.play(o, 0));
        assertEquals("Not your turn", ex.getMessage());
    }

    @Test
    @DisplayName("play rejeita posicao ocupada")
    void playRejectsTakenPosition() {
        game.play(x, 0);
        InputInvalidException ex = assertThrows(InputInvalidException.class, () -> game.play(o, 0));
        assertEquals("Position taken", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XXX------", "---XXX---", "------XXX",
            "X--X--X--", "-X--X--X-", "--X--X--X",
            "X---X---X", "--X-X-X--"
    })
    @DisplayName("winner detecta todas as linhas vencedoras do X")
    void winnerDetectsAllLinesForX(String board) {
        game.setBoard(board);
        assertEquals(Mark.X, game.winner());
        assertFalse(game.isDraw());
    }

    @Test
    @DisplayName("winner detecta vitoria do O")
    void winnerDetectsO() {
        game.setBoard("OOOXX-X--");
        assertEquals(Mark.O, game.winner());
    }

    @Test
    @DisplayName("isDraw quando tabuleiro cheio sem vencedor")
    void drawWhenBoardFull() {
        game.setBoard("XOXXOOOXX");
        assertNull(game.winner());
        assertTrue(game.isDraw());
    }

    @Test
    @DisplayName("partida completa ate vitoria do X")
    void fullGameXWins() {
        game.play(x, 0);
        game.play(o, 3);
        game.play(x, 1);
        game.play(o, 4);
        game.play(x, 2);
        assertEquals(Mark.X, game.winner());
    }

    @Test
    @DisplayName("getters e setters")
    void gettersAndSetters() {
        UUID id = UUID.randomUUID();
        Room other = Room.init("Outra", o);
        game.setId(id);
        game.setRoom(other);
        game.setXPlayerId(o);
        game.setOPlayerId(x);
        game.setTurn(Mark.O);
        game.setVersion(3L);
        assertEquals(id, game.getId());
        assertSame(other, game.getRoom());
        assertEquals(o, game.getXPlayerId());
        assertEquals(x, game.getOPlayerId());
        assertEquals(Mark.O, game.getTurn());
        assertEquals(3L, game.getVersion());
        assertEquals(2, Mark.values().length);
        assertEquals(Mark.O, Mark.valueOf("O"));
    }
}
