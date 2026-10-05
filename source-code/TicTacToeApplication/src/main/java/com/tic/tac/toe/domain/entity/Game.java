package com.tic.tac.toe.domain.entity;

import com.tic.tac.toe.domain.entity.enums.Mark;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "room_id", nullable = false, unique = true, updatable = false)
    private Room room;

    // 9 posições: '-' vazio, 'X' ou 'O'. Ex: "X-O------"
    @Column(name = "board", nullable = false, length = 9)
    private String board;

    @Column(name = "x_player_id", nullable = false)
    private UUID xPlayerId;

    @Column(name = "o_player_id", nullable = false)
    private UUID oPlayerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "turn", nullable = false)
    private Mark turn;

    @Version
    private long version; // evita jogadas concorrentes conflitantes

    protected Game() {}

    public static Game init(Room room, UUID xPlayerId, UUID oPlayerId) {
        Game g = new Game();
        g.id = UUID.randomUUID();
        g.room = room;
        g.board = "---------";
        g.xPlayerId = xPlayerId;
        g.oPlayerId = oPlayerId;
        g.turn = Mark.X;
        return g;
    }

    public void play(UUID playerId, int pos) {
        if (pos < 0 || pos > 8) throw new InputInvalidException("Invalid position");
        UUID expected = turn == Mark.X ? xPlayerId : oPlayerId;
        if (!expected.equals(playerId)) throw new InputInvalidException("Not your turn");
        if (board.charAt(pos) != '-') throw new InputInvalidException("Position taken");
        char[] b = board.toCharArray();
        b[pos] = turn == Mark.X ? 'X' : 'O';
        board = new String(b);
        turn = turn == Mark.X ? Mark.O : Mark.X;
    }

    public Mark winner() {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] l : lines) {
            char c = board.charAt(l[0]);
            if (c != '-' && c == board.charAt(l[1]) && c == board.charAt(l[2])) {
                return c == 'X' ? Mark.X : Mark.O;
            }
        }
        return null;
    }

    public boolean isDraw() {
        return winner() == null && board.indexOf('-') < 0;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public UUID getXPlayerId() {
        return xPlayerId;
    }

    public void setXPlayerId(UUID xPlayerId) {
        this.xPlayerId = xPlayerId;
    }

    public UUID getOPlayerId() {
        return oPlayerId;
    }

    public void setOPlayerId(UUID oPlayerId) {
        this.oPlayerId = oPlayerId;
    }

    public Mark getTurn() {
        return turn;
    }

    public void setTurn(Mark turn) {
        this.turn = turn;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
