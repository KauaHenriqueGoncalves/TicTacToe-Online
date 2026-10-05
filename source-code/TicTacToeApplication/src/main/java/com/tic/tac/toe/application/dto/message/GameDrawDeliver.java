package com.tic.tac.toe.application.dto.message;

public final class GameDrawDeliver {
    private final String message;

    public GameDrawDeliver(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
