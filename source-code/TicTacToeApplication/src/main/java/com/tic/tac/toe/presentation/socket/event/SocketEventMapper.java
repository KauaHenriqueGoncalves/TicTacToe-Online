package com.tic.tac.toe.presentation.socket.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.*;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.message.SocketMessageReceive;
import java.util.UUID;

public final class SocketEventMapper {
    private SocketEventMapper() {
    }

    public static DomainEvent toDomainEvent(SocketMessageReceive request, Connection connection) {
        String eventName = request.getEvent();
        JsonNode content = request.getContent();
        switch (eventName) {
            case "global.message":
                return globalMessage(content, connection);
            case "global.rooms.by.status":
                return globalRooms(content, connection);
            case "room.create":
                return roomCreate(content, connection);
            case "room.message":
                return roomMessage(content, connection);
            case "room.join":
                return joinRoom(content, connection);
            case "room.info":
                return infoRoom(content, connection);
            case "room.leave":
                return leaveRoom(content, connection);
            case "room.ready":
                return readyRoom(content, connection);
            case "room.game.start":
                return startGame(content, connection);
            default:
                throw new InputInvalidException("Unknown event: " + eventName);
        }
    }

    private static DomainEvent globalMessage(JsonNode content, Connection connection) {
        return new GlobalMessageEvent(connection.getUserId(), content.get("message").asText());
    }

    private static DomainEvent globalRooms(JsonNode content, Connection connection) {
        return new RoomsByStatusEvent(RoomStatus.fromString(content.get("status").asText()), connection.getUserId(), false);
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

    private static DomainEvent joinRoom(JsonNode content, Connection connection) {
        return new JoinRoomEvent(UUID.fromString(content.get("roomId").asText()), connection.getUserId());
    }

    private static DomainEvent infoRoom(JsonNode content, Connection connection) {
        return new InfoRoomEvent(connection.getRoomId(), connection.getUserId());
    }

    private static DomainEvent leaveRoom(JsonNode content, Connection connection) {
        return new LeaveRoomEvent(connection.getRoomId().toString(), connection.getUserId().toString());
    }

    private static DomainEvent readyRoom(JsonNode content, Connection connection) {
        return new ReadyRoomEvent(connection.getRoomId().toString(), connection.getUserId().toString());
    }

    private static DomainEvent startGame(JsonNode content, Connection connection) {
        return new StartGameEvent(connection.getRoomId(), connection.getUserId());
    }
}
