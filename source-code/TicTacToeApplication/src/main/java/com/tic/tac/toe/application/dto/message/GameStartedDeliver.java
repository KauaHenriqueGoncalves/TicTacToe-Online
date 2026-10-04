package com.tic.tac.toe.application.dto.message;

public final class GameStartedDeliver {
    private final String board;
    private final String xPlayerId;
    private final String oPlayerId;
    private final String turn;

    public GameStartedDeliver(String board, String xPlayerId, String oPlayerId, String turn) {
        this.board = board;
        this.xPlayerId = xPlayerId;
        this.oPlayerId = oPlayerId;
        this.turn = turn;
    }

    public String getBoard() {
        return board;
    }

    public String getXPlayerId() {
        return xPlayerId;
    }

    public String getOPlayerId() {
        return oPlayerId;
    }

    public String getTurn() {
        return turn;
    }
}
