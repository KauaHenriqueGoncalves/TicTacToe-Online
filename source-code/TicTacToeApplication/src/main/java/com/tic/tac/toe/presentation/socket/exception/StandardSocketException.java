package com.tic.tac.toe.presentation.socket.exception;

import java.time.Instant;

public final class StandardSocketException {
    private Instant timestamp;
    private String event;
    private String error;
    private String message;
    private String fromEvent;

    public StandardSocketException() {
    }

    public StandardSocketException(Instant timestamp, String event, String error, String message, String fromEvent) {
        this.timestamp = timestamp;
        this.event = event;
        this.error = error;
        this.message = message;
        this.fromEvent = fromEvent;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getEvent() {
        return event;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getFromEvent() {
        return fromEvent;
    }
}
