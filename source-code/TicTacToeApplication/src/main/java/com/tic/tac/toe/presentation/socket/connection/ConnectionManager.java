package com.tic.tac.toe.presentation.socket.connection;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectionManager {
    private static final Logger log =
            LoggerFactory.getLogger(ConnectionManager.class);
    private final Set<Connection> connections = ConcurrentHashMap.newKeySet();

    public ConnectionManager() {
        log.info("SocketManager successfully instantiated.");
    }

    public void add(Connection c) {
        connections.add(c);
    }

    public void remove(Connection c) {
        if (connections.contains(c)) {
            // throw
        }
        connections.remove(c);
    }

    public void removeByConnection(WebSocket ws) {
        for (Connection connection : connections) {
            if (connection.getConnection().equals(ws)) {
                connections.remove(connection);
                return;
            }
        }
        // thorw
    }

    public Connection getByUserId(UUID userId) {
        return connections.stream()
                .filter(c -> c.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public Connection getByConnection(WebSocket ws) {
        return connections.stream()
                .filter(c -> c.getConnection().equals(ws))
                .findFirst()
                .orElse(null);
    }

    public void broadcast(String message) {
        connections.forEach(conn -> {
            conn.getConnection().send(message);
        });
    }
}
