package com.tic.tac.toe.domain.event;

import java.util.UUID;

public final class CreateRoomEvent implements DomainEvent {
    private final String name;
    private final UUID ownerId;

    public CreateRoomEvent(String name, UUID ownerId) {
        this.name = name;
        this.ownerId = ownerId;
    }

    @Override
    public String getEvent() {
        return "room.create";
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}
