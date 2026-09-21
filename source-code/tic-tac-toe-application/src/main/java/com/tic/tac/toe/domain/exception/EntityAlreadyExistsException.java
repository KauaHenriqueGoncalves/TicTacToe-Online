package com.tic.tac.toe.domain.exception;

public final class EntityAlreadyExistsException extends CustomException {
    public EntityAlreadyExistsException(String message) {
        super(409, "ENTITY_ALREADY_EXISTS", message);
    }
}
