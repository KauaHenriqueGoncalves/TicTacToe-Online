package com.tic.tac.toe.presentation.socket.connection;

import org.java_websocket.WebSocket;
import java.util.Objects;
import java.util.UUID;

public final class Connection {
    private final UUID id;
    private final WebSocket connection;
    private final UUID userId;
    private UUID roomId;

    private Connection(UUID id, WebSocket connection, UUID userId) {
        this.id = id;
        this.connection = connection;
        this.userId = userId;
        this.roomId = null;
    }

    public static Connection create(WebSocket connection, UUID userId) {
        if (connection == null) {
            throw new RuntimeException("Connection is required");
        }
        if (userId == null || userId.toString().isEmpty()) {
            throw new RuntimeException("UserId is required");
        }
        return new Connection(
                UUID.randomUUID(),
                connection,
                userId
        );
    }

    public UUID getId() {
        return id;
    }

    public WebSocket getConnection() {
        return connection;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
