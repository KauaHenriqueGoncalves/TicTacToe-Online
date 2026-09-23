package com.tic.tac.toe.presentation.socket.room;

import com.tic.tac.toe.presentation.socket.connection.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.UUID;

public final class RoomManager {
    private static final Logger log = LoggerFactory.getLogger(RoomManager.class);
    private final Set<Room> rooms;

    public RoomManager(Set<Room> rooms) {
        this.rooms = rooms;
        log.info("Instance {} initialized.", RoomManager.class.getSimpleName());
    }

    public void create(UUID id) {
        rooms.add(Room.create(id));
    }

    public Room get(UUID id) {
        for (Room room : rooms) {
            if (room.getId().equals(id)) {
                return room;
            }
        }
        throw new RuntimeException("error");
    }

    public void join(UUID roomId, Connection socket) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                room.getUsers().put(socket.getUserId(), socket.getConnection());
                socket.setRoomId(roomId);
                return;
            }
        }
    }

    public void leave(UUID roomId, Connection connection) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                for (UUID userId : room.getUsers().keySet()) {
                    if (userId.equals(connection.getUserId())) {
                        room.getUsers().remove(userId);
                        connection.setRoomId(null);
                        return;
                    }
                }
            }
        }
        // throw user não pertence a room
    }

    public void broadcast(UUID roomId, String message) {
        Room room = get(roomId);
        room.getUsers().values().forEach(connection -> {
            connection.send(message);
        });
    }

    public void remove(Room room) {
        rooms.remove(room);
    }

    public void removeById(UUID roomId) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                rooms.remove(room);
                return;
            }
        }
        //thorw not found
    }
}
