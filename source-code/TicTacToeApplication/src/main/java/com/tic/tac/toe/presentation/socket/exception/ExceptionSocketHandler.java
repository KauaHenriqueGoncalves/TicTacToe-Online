package com.tic.tac.toe.presentation.socket.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tic.tac.toe.domain.exception.EntityAlreadyExistsException;
import com.tic.tac.toe.presentation.http.exception.ExceptionHttpHandler;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;

public final class ExceptionSocketHandler {
    private static final Logger log =
            LoggerFactory.getLogger(ExceptionSocketHandler.class);
    private static final ExceptionSocketHandler FACTORY;
    private final ObjectMapper objectMapper;

    static {
        FACTORY = new ExceptionSocketHandler(new ObjectMapper());
        log.info("Instance {} initialized.", ExceptionHttpHandler.class.getSimpleName());
    }

    private ExceptionSocketHandler(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper = objectMapper;
    }

    public static ExceptionSocketHandler getFactory() {
        return FACTORY;
    }

    public void handle(WebSocket conn, String event, Exception exception) {
        if (exception instanceof EntityAlreadyExistsException) {
            StandardSocketException standard = new StandardSocketException(
                    Instant.now(),
                    "ERROR",
                    ((EntityAlreadyExistsException) exception).getError(),
                    exception.getMessage(),
                    event
            );
            sendTo(conn, standard);
            return;
        }

        if (exception instanceof RuntimeException) {
            StandardSocketException standard = new StandardSocketException(
                    Instant.now(),
                    "ERROR",
                    "INTERNET_SERVER_ERROR",
                    exception.getMessage(),
                    event
            );
            sendTo(conn, standard);
            return;
        }
    }

    private void sendTo(WebSocket conn, StandardSocketException ex) {
        try {
            String message =
                    objectMapper.writeValueAsString(ex);
            conn.send(message);
        } catch (JsonProcessingException e) {
            log.error("Error on transform StandardSocketException in json.");
            throw new RuntimeException("Error on transform StandardSocketException in json.");
        }
    }
}
