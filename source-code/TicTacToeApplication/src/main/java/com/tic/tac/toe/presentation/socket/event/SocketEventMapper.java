package com.tic.tac.toe.presentation.socket.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.domain.event.CreateRoomEvent;
import com.tic.tac.toe.domain.event.DomainEvent;
import com.tic.tac.toe.domain.event.GlobalMessageEvent;
import com.tic.tac.toe.domain.event.RoomMessageEvent;
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
            case "global.message":
                return globalMessage(content, connection);
            case "room.create":
                return roomCreate(content, connection);
            case "room.message":
                return roomMessage(content, connection);
            default:
                throw new InputInvalidException("Unknown event: " + eventName);
        }
    }

    private static DomainEvent globalMessage(JsonNode content, Connection connection) {
        return new GlobalMessageEvent(connection.getUserId(), content.get("message").asText());
    }

    private static DomainEvent roomCreate(JsonNode content, Connection connection) {
        return new CreateRoomEvent(content.get("name").asText(), connection.getUserId());
    }

    private static DomainEvent roomMessage(JsonNode content, Connection connection) {
        return new RoomMessageEvent(
                connection.getUserId(),
                connection.getRoomId(),
                content.get("message").asText()
        );
    }
}
