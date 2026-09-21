package com.tic.tac.toe.domain.exception;

public final class InputInvalidException extends CustomException {
    public InputInvalidException(String message) {
        super(400, "INPUT_INVALID", message);
    }
}
