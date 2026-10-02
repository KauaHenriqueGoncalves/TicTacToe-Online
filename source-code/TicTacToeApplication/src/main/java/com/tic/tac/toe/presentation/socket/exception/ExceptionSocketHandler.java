package com.tic.tac.toe.presentation.socket.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tic.tac.toe.domain.exception.*;
import com.tic.tac.toe.presentation.http.exception.ExceptionHttpHandler;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;

public final class ExceptionSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ExceptionSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExceptionSocketHandler() {
        objectMapper.registerModule(new JavaTimeModule());
        log.info("Instance {} initialized. [InstanceId={}]",
                ExceptionSocketHandler.class.getSimpleName(), System.identityHashCode(this));
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

        if (exception instanceof InputInvalidException) {
            StandardSocketException standard = new StandardSocketException(
                    Instant.now(),
                    "ERROR",
                    ((InputInvalidException) exception).getError(),
                    exception.getMessage(),
                    event
            );
            sendTo(conn, standard);
            return;
        }

        if (exception instanceof NotFoundException) {
            StandardSocketException standard = new StandardSocketException(
                    Instant.now(),
                    "ERROR",
                    ((NotFoundException) exception).getError(),
                    exception.getMessage(),
                    event
            );
            sendTo(conn, standard);
            return;
        }

        if (exception instanceof RepositoryException) {
            StandardSocketException standard = new StandardSocketException(
                    Instant.now(),
                    "ERROR",
                    ((RepositoryException) exception).getError(),
                    exception.getMessage(),
                    event
            );
            sendTo(conn, standard);
            return;
        }

        if (exception instanceof UnauthorizedException) {
            StandardSocketException standard = new StandardSocketException(
                    Instant.now(),
                    "ERROR",
                    ((UnauthorizedException) exception).getError(),
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
