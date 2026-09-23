package com.tic.tac.toe.presentation.socket.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.domain.event.DomainEvent;
import com.tic.tac.toe.domain.event.MessageEvent;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.message.SocketMessageReceive;
import java.util.UUID;

public final class SocketEventMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private SocketEventMapper() {
    }

    public static DomainEvent toDomainEvent(SocketMessageReceive request, Connection connection) {
        String eventName = request.getEvent();
        JsonNode content = request.getContent();
        switch (eventName) {
            case "message":
                return message(content, connection);
            default:
                throw new InputInvalidException("Unknown event: " + eventName);
        }
    }

    private static DomainEvent message(JsonNode content, Connection connection) {
        UUID roomId = UUID.randomUUID();
        return new MessageEvent(connection.getUserId(), roomId);
    }

}
