package com.tic.tac.toe.domain.exception;

public final class RepositoryException extends CustomException {
    public RepositoryException(String message) {
        super(500, "ERROR_REPOSITORY", message);
    }
}
