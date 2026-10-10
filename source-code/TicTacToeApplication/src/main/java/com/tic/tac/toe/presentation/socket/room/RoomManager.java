package com.tic.tac.toe.presentation.socket.room;

import com.tic.tac.toe.presentation.socket.connection.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RoomManager {
    private static final Logger log = LoggerFactory.getLogger(RoomManager.class);
    private final Set<Room> rooms;

    public RoomManager() {
        this.rooms = ConcurrentHashMap.newKeySet();
        log.info("Instance {} initialized. [InstanceId={}]",
                RoomManager.class.getSimpleName(), System.identityHashCode(this));
    }

    public RoomManager(Set<Room> rooms) {
        this.rooms = rooms;
        log.info("Instance {} initialized. [InstanceId={}] [SetClass={}]",
                RoomManager.class.getSimpleName(), System.identityHashCode(this), rooms.getClass());
    }

    public Room create(UUID id) {
        Room room = Room.create(id);
        rooms.add(room);
        log.info("Room created. [RoomId={}] [TotalRooms={}]",
                room.getId(), rooms.size());
        return room;
    }

    public Room get(UUID id) {
        for (Room room : rooms) {
            if (room.getId().equals(id)) {
                return room;
            }
        }
        log.warn("Room not found. [RoomId={}]", id);
        throw new RuntimeException("Room not found: " + id);
    }

    public void join(UUID roomId, Connection socket) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                room.getUsers().put(socket.getUserId(), socket.getConnection());
                socket.setRoomId(roomId);
                log.info("User joined room. [RoomId={}] [UserId={}] [TotalUsers={}]",
                        roomId, socket.getUserId(), room.getUsers().size());
                return;
            }
        }
        log.warn("Join failed: room not found. [RoomId={}] [UserId={}]",
                roomId, socket.getUserId());
    }

    public void leave(UUID roomId, Connection connection) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                if (room.getUsers().remove(connection.getUserId()) != null) {
                    connection.setRoomId(null);
                    log.info("User left room. [RoomId={}] [UserId={}] [TotalUsers={}]",
                            roomId, connection.getUserId(), room.getUsers().size());
                } else {
                    log.warn("Leave failed: user not in room. [RoomId={}] [UserId={}]]",
                            roomId, connection.getUserId());
                }
                return;
            }
        }
        log.warn("Leave failed: room not found. [RoomId={}] [UserId={}]",
                roomId, connection.getUserId());
    }

    public void broadcast(UUID roomId, String message) {
        Room room = get(roomId);
        room.getUsers().values().forEach(connection -> connection.send(message));
        log.info("Broadcast sent. [RoomId={}] [Recipients={}]",
                roomId, room.getUsers().size());
    }

    public void remove(Room room) {
        boolean removed = rooms.remove(room);
        log.info("Room removed. [RoomId={}] [Removed={}] [TotalRooms={}]",
                room.getId(), removed, rooms.size());
    }

    public void removeById(UUID roomId) {
        Optional<Room> target = rooms.stream()
                .filter(room -> room.getId().equals(roomId))
                .findFirst();
        if (target.isPresent()) {
            rooms.remove(target.get());
            log.info("Room removed by id. [RoomId={}] [TotalRooms={}]",
                    roomId, rooms.size());
        } else {
            log.warn("Remove failed: room not found. [RoomId={}]", roomId);
        }
    }
}
