package com.tic.tac.toe.presentation.http.exception;

import java.time.Instant;

public final class StandardHttpException {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public StandardHttpException() {
    }

    public StandardHttpException(Instant timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
