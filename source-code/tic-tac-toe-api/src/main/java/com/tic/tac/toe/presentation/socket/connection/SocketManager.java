package com.tic.tac.toe.presentation.socket.connection;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SocketManager {
    private static final Logger log =
            LoggerFactory.getLogger(SocketManager.class);
    private final Set<SocketConnection> connections = ConcurrentHashMap.newKeySet();

    public SocketManager() {
        log.info("SocketManager successfully instantiated.");
    }

    public void add(SocketConnection c) {
        connections.add(c);
    }

    public void remove(SocketConnection c) {
        if (connections.contains(c)) {
            // throw
        }
        connections.remove(c);
    }

    public void removeByConnection(WebSocket ws) {
        for (SocketConnection connection : connections) {
            if (connection.getConnection().equals(ws)) {
                connections.remove(connection);
                return;
            }
        }
        // thorw
    }

    public SocketConnection getByUserId(UUID userId) {
        return connections.stream()
                .filter(c -> c.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public SocketConnection getByConnection(WebSocket ws) {
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
