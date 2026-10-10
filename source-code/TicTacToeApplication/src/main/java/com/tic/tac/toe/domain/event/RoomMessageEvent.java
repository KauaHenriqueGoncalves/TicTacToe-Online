package com.tic.tac.toe.domain.event;

import java.util.UUID;

public final class RoomMessageEvent implements DomainEvent {
    private final UUID userId;
    private final UUID roomId;
    private final String message;

    public RoomMessageEvent(UUID userId, UUID roomId, String message) {
        this.userId = userId;
        this.roomId = roomId;
        this.message = message;
    }

    @Override
    public String getEvent() {
        return "room.message";
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getMessage() {
        return message;
    }
}
