package com.tic.tac.toe.domain.exception;

public final class UnauthorizedException extends CustomException{
    public UnauthorizedException(String message) {
        super(401, "UNAUTHORIZED", message);
    }
}
