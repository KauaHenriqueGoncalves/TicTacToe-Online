package com.tic.tac.toe.domain.event;

import java.util.UUID;

public final class PlayGameEvent implements DomainEvent {
    private final UUID roomId;
    private final UUID userId;
    private final int position;

    public PlayGameEvent(UUID roomId, UUID userId, int position) {
        this.roomId = roomId;
        this.userId = userId;
        this.position = position;
    }

    @Override
    public String getEvent() {
        return "room.game.play";
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getPosition() {
        return position;
    }
}
