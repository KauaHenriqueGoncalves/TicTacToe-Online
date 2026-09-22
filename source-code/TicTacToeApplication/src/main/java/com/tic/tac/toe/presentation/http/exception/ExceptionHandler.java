package com.tic.tac.toe.presentation.http.exception;

import com.tic.tac.toe.domain.exception.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.time.Instant;

public final class ExceptionHandler {
    private ExceptionHandler() {
    }

    private static StandardException sent(
            CustomException ex,
            Context ctx
    ) {
        return new StandardException(
                Instant.now(),
                ex.getStatus(),
                ex.getError(),
                ex.getMessage(),
                ctx.path()
        );
    };

    public static void register(Javalin server) {
        server.exception(EntityAlreadyExistsException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(ExceptionHandler.sent(ex, ctx));
        });

        server.exception(InputInvalidException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(ExceptionHandler.sent(ex, ctx));
        });

        server.exception(NotFoundException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(ExceptionHandler.sent(ex, ctx));
        });

        server.exception(RepositoryException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(ExceptionHandler.sent(ex, ctx));
        });

        server.exception(UnauthorizedException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(ExceptionHandler.sent(ex, ctx));
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
