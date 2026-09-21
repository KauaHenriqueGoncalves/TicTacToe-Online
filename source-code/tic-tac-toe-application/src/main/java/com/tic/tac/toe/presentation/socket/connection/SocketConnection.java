package com.tic.tac.toe.presentation.socket.connection;

import org.java_websocket.WebSocket;
import java.util.Objects;
import java.util.UUID;

public final class SocketConnection {
    private final UUID id;
    private final WebSocket connection;
    private final UUID userId;
    private UUID roomId;

    private SocketConnection(UUID id, WebSocket connection, UUID userId) {
        this.id = id;
        this.connection = connection;
        this.userId = userId;
    }

    public static SocketConnection create(WebSocket connection, UUID userId) {
        if (connection == null) {
            throw new RuntimeException("informar connection");
        }
//        if (accessToken == null || accessToken.isEmpty()) {
//            throw new RuntimeException("accessToken é obrigatório");
//        }
        return new SocketConnection(
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
        SocketConnection that = (SocketConnection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
