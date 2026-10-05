package com.tic.tac.toe.application.dto.message;

public final class LeaveRoomSuccessfulDeliver {
    private final String message;

    public LeaveRoomSuccessfulDeliver(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
