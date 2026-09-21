package com.tic.tac.toe;

import com.tic.tac.toe.presentation.http.HttpServer;
import com.tic.tac.toe.presentation.socket.SocketServer;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Application {
    private static final Logger log =
            LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            SocketServer socketServer = SocketServer.getServer();
            Javalin httpServer = HttpServer.getServer();
        } catch (RuntimeException e) {
            log.error("Error during application startup. [error={}]", e.getMessage());
            System.exit(0);
        } finally {
            log.info("Started Application");
        }
    }
}
