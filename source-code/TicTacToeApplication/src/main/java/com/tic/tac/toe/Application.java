package com.tic.tac.toe;

import com.tic.tac.toe.presentation.http.HttpServer;
import com.tic.tac.toe.presentation.socket.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            AppContext appContext = AppContext.getInstance();
            SocketServer.start(appContext);
            HttpServer.start(appContext);
        } catch (RuntimeException e) {
            log.error("Error during application startup. [error={}]", e.getMessage());
            System.exit(0);
        } finally {
            log.info("Started Application");
        }
    }
}
