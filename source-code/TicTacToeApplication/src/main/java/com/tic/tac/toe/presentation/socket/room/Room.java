package com.tic.tac.toe.presentation.socket.room;

import org.java_websocket.WebSocket;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Room {
    private final UUID id;
    private final Map<UUID, WebSocket> users = new ConcurrentHashMap<>();

    private Room(UUID id) {
        this.id = id;
    }

    public static Room create(UUID id) {
        if (id == null) {
            throw new RuntimeException("Id is requeried to create a room.");
        }
        return new Room(id);
    }

    public UUID getId() {
        return id;
    }

    public Map<UUID, WebSocket> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
