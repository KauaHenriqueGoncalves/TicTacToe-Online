package com.tic.tac.toe.application.dto.message;

public final class GameWinnerDeliver {
    private final String from;
    private final String winnerId;

    public GameWinnerDeliver(String from, String winnerId) {
        this.from = from;
        this.winnerId = winnerId;
    }

    public String getFrom() {
        return from;
    }

    public String getWinnerId() {
        return winnerId;
    }
}
