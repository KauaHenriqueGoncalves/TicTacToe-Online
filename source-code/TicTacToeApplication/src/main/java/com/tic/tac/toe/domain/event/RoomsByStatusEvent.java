package com.tic.tac.toe.domain.event;

import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import java.util.UUID;

public final class RoomsByStatusEvent implements DomainEvent {
    private final RoomStatus status;
    private final UUID userId;
    private final boolean isBroadcast;

    public RoomsByStatusEvent(RoomStatus status, UUID userId, boolean isBroadcast) {
        this.status = status;
        this.userId = userId;
        this.isBroadcast = isBroadcast;
    }

    @Override
    public String getEvent() {
        return "global.rooms.by.status";
    }

    public RoomStatus getStatus() {
        return status;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }
}
