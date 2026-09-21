package com.tic.tac.toe.presentation.http.exception;

import com.tic.tac.toe.domain.exception.*;
import io.javalin.Javalin;
import java.time.Instant;

public final class ExceptionHandler {
    private ExceptionHandler() {
    }

    public static void register(Javalin server) {
        server.exception(EntityAlreadyExistsException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(new StandardException(
                    Instant.now(),
                    ex.getStatus(),
                    ex.getError(),
                    ex.getMessage(),
                    ctx.path()
            ));
        });

        server.exception(InputInvalidException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(new StandardException(
                    Instant.now(),
                    ex.getStatus(),
                    ex.getError(),
                    ex.getMessage(),
                    ctx.path()
            ));
        });

        server.exception(NotFoundException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(new StandardException(
                    Instant.now(),
                    ex.getStatus(),
                    ex.getError(),
                    ex.getMessage(),
                    ctx.path()
            ));
        });

        server.exception(RepositoryException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(new StandardException(
                    Instant.now(),
                    ex.getStatus(),
                    ex.getError(),
                    ex.getMessage(),
                    ctx.path()
            ));
        });

        server.exception(UnauthorizedException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(new StandardException(
                    Instant.now(),
                    ex.getStatus(),
                    ex.getError(),
                    ex.getMessage(),
                    ctx.path()
            ));
        });

        server.exception(RuntimeException.class, (ex, ctx) -> {
            ctx.status(500);
            ctx.json(new StandardException(
                    Instant.now(),
                    500,
                    "INTERNET_SERVER_ERROR",
                    ex.getMessage(),
                    ctx.path()
            ));
        });
    }
}
