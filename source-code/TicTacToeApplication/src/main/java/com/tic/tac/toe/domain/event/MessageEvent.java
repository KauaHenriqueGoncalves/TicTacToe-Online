package com.tic.tac.toe.domain.event;

import java.util.UUID;

public final class MessageEvent implements DomainEvent {
    private final UUID userId;
    private final UUID roomId;

    public MessageEvent(UUID userId, UUID roomId) {
        this.userId = userId;
        this.roomId = roomId;
    }

    @Override
    public String getEvent() {
        return "message";
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoomId() {
        return roomId;
    }
}
