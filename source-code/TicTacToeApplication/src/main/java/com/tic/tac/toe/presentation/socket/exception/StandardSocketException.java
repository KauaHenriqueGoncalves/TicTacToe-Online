package com.tic.tac.toe.presentation.socket.exception;

import java.time.Instant;

public final class StandardSocketException {
    private Instant timestamp;
    private String error;
    private String message;
    private String event;

    public StandardSocketException() {
    }

    public StandardSocketException(Instant timestamp, String error, String message, String event) {
        this.timestamp = timestamp;
        this.error = error;
        this.message = message;
        this.event = event;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getEvent() {
        return event;
    }
}
