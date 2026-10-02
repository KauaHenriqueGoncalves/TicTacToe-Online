package com.tic.tac.toe.application.dto.request;

import java.util.UUID;

public final class CreateRoomRequestDto {
    private String name;
    private UUID ownerId;

    public CreateRoomRequestDto() {
    }

    public CreateRoomRequestDto(String name, UUID ownerId) {
        this.name = name;
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    @Override
    public String toString() {
        return "CreateRoomRequestDto{" +
                "name='" + name + '\'' +
                ", ownerId=" + ownerId +
                '}';
    }
}
