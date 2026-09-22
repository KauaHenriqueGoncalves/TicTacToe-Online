package com.tic.tac.toe.presentation.http;

import com.tic.tac.toe.infrastructure.config.Environment;
import com.tic.tac.toe.presentation.http.controller.AuthController;
import com.tic.tac.toe.presentation.http.exception.ExceptionHandler;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpServer {
    private static final Logger log =
            LoggerFactory.getLogger(HttpServer.class);
    private static final Javalin server;
    private static final int PORT;
    private static final String PREFIX;

    static {
        PORT = Integer.parseInt(Environment.get("HTTP_PORT"));
        PREFIX = Environment.get("HTTP_PREFIX");
        server = Javalin.create(config -> {
            config.enableCorsForOrigin("*");
            config.showJavalinBanner = false;
        }).start(PORT);
        server.before(ctx -> {
            log.info("METHOD: {}, HOST: {}, PATH: {}, CONTENT_LENGTH: {}",
                    ctx.method(), ctx.host(), ctx.path(), ctx.contentLength());
        });
        server.routes(() -> {
            ExceptionHandler.register(server);
            AuthController.register(server, PREFIX);
        });
        log.info("Instance {} initialized.", HttpServer.class.getSimpleName());
        log.info("HttpServer started successfully. [port={}] [url={}]",
                PORT, "http://localhost:" + PORT);
    }

    public static Javalin getServer() {
        return server;
    }
}
