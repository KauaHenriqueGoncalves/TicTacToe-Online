package com.tic.tac.toe.application.dto.response;

import java.util.UUID;

public final class RoomPlayerInfoRoomResponseDto {
    private final UUID id;
    private final UUID userId;
    private final String username;
    private final Boolean isPlaying;
    private final Boolean ready;

    public RoomPlayerInfoRoomResponseDto(UUID id, UUID userId, String username, Boolean isPlaying, Boolean ready) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.isPlaying = isPlaying;
        this.ready = ready;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Boolean getPlaying() {
        return isPlaying;
    }

    public Boolean getReady() {
        return ready;
    }
}
