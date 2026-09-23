package com.tic.tac.toe.presentation.http;

import com.tic.tac.toe.AppContext;
import com.tic.tac.toe.infrastructure.config.Environment;
import com.tic.tac.toe.presentation.http.exception.ExceptionHttpHandler;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpServer {
    private static final Logger log =
            LoggerFactory.getLogger(HttpServer.class);

    public static void start(AppContext context) {
        int port = Integer.parseInt(Environment.get("HTTP_PORT"));
        String prefix = Environment.get("HTTP_PREFIX");
        Javalin server = Javalin.create(
                config -> {
                    config.enableCorsForOrigin("*");
                    config.showJavalinBanner = false;
                }
        ).start(port);
        server.before(ctx -> {
            log.info("METHOD: {}, HOST: {}, PATH: {}, CONTENT_LENGTH: {}",
                    ctx.method(), ctx.host(), ctx.path(), ctx.contentLength());
        });
        server.routes(() -> {
            ExceptionHttpHandler.register(server);
            context.authController.register(server, prefix);
        });
        log.info("Instance {} initialized.", HttpServer.class.getSimpleName());
        log.info("HttpServer started successfully. [port={}] [url={}]",
                port, "http://localhost:" + port);
    }
}
