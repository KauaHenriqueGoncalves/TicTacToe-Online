package com.tic.tac.toe.domain.event;

import java.util.UUID;

public final class StartGameEvent implements DomainEvent {
    private final UUID roomId;
    private final UUID userId;

    public StartGameEvent(UUID roomId, UUID userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

    @Override
    public String getEvent() {
        return "room.game.start";
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getUserId() {
        return userId;
    }
}
