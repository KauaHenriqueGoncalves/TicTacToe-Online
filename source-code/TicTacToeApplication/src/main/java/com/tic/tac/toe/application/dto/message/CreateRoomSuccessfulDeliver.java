package com.tic.tac.toe.application.dto.message;

public final class CreateRoomSuccessfulDeliver {
    private final String message;

    public CreateRoomSuccessfulDeliver(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
