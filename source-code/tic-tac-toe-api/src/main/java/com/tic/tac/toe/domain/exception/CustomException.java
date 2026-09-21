package com.tic.tac.toe.domain.exception;

public class CustomException extends RuntimeException {
    private final int status;
    private final String error;

    public CustomException(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
