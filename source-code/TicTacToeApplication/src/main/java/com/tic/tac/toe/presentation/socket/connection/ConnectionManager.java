package com.tic.tac.toe.presentation.socket.connection;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.UUID;

public final class ConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);
    private final Set<Connection> connections;

    public ConnectionManager(Set<Connection> connections) {
        this.connections = connections;
        log.info("Instance {} initialized.", ConnectionManager.class.getSimpleName());
    }

    public void add(Connection c) {
        if (c == null) {
            log.warn("Connection is required.");
            throw new RuntimeException("Connection is required.");
        }
        if (c.getId() == null) {
            log.warn("ConnectionId is required.");
            throw new RuntimeException("ConnectionId is required.");
        }
        if (c.getConnection() == null) {
            log.warn("WebSocketConnection is required.");
            throw new RuntimeException("WebSocketConnection is required.");
        }
        if (c.getUserId() == null) {
            log.warn("UserId is required.");
            throw new RuntimeException("UserId is required.");
        }
        connections.add(c);
        log.info("A connection added on manager. [connectionId={}] [userId={}]",
                c.getId(), c.getUserId());
    }

    public void remove(Connection c) {
        if (!connections.contains(c)) {
            log.warn("Connection not found on manager.");
            throw new RuntimeException("Error on connection manager.");
        }
        connections.remove(c);
        log.info("Connection removed successfully. [connectionId={}] [userId={}]",
                c.getId(), c.getUserId());
    }

    public void removeByConnection(WebSocket ws) {
        for (Connection c : connections) {
            if (c.getConnection().equals(ws)) {
                connections.remove(c);
                log.info("Connection removed with by websocket successfully. [connectionId={}] [userId={}]",
                        c.getId(), c.getUserId());
                return;
            }
        }
        log.warn("Connection by websocket not found on manager.");
        throw new RuntimeException("Error on connection manager.");
    }

    public Connection getByUserId(UUID userId) {
        log.info("Getting connection by userId. [userId={}]", userId);
        Connection connection = connections.stream()
                .filter(c -> c.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        if (connection == null) {
            log.warn("Not found connection by userId");
            return null;
        }
        log.info("Got connection by userId. [connectionId={}] [userId={}]",
                connection.getId(), connection.getUserId());
        return connection;
    }

    public Connection getByConnection(WebSocket ws) {
        log.info("Getting connection by websocket");
        Connection connection = connections.stream()
                .filter(c -> c.getConnection().equals(ws))
                .findFirst()
                .orElse(null);
        if (connection == null) {
            log.warn("Not found connection by websocket");
            return null;
        }
        log.info("Got connection by websocket. [connectionId={}] [userId={}]",
                connection.getId(), connection.getUserId());
        return connection;
    }

    public void broadcast(String message) {
        log.info("Sending message on broadcast");
        connections.forEach(conn -> {
            log.info("Send message to connection. [connectionId={}] [userId={}]",
                    conn.getId(), conn.getUserId());
            conn.getConnection().send(message);
        });
    }
}
