package com.tic.tac.toe.domain.exception;

public final class NotFoundException extends CustomException {
    public NotFoundException(String message) {
        super(404, "NOT_FOUND", message);
    }
}
