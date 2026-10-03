package com.tic.tac.toe.domain.event;

import java.util.UUID;

public final class JoinRoomEvent implements DomainEvent {
    private final UUID roomId;
    private final UUID userId;

    public JoinRoomEvent(UUID roomId, UUID userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

    @Override
    public String getEvent() {
        return "room.join";
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getUserId() {
        return userId;
    }
}
