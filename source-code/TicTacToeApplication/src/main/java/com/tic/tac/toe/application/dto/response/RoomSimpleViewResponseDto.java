package com.tic.tac.toe.application.dto.response;

import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RoomSimpleViewResponseDto {
    private UUID id;
    private String name;
    private RoomStatus status;
    private Integer currentPlayer;
    private Integer maxPlayers;
    private LocalDateTime createdAt;

    public RoomSimpleViewResponseDto() {
    }

    public RoomSimpleViewResponseDto(UUID id, String name, RoomStatus status, Integer currentPlayer, Integer maxPlayers, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.currentPlayer = currentPlayer;
        this.maxPlayers = maxPlayers;
        this.createdAt = createdAt;
    }

    public static List<RoomSimpleViewResponseDto> of(List<Room> r, Map<UUID, Long> counts) {
        return r.stream().map(room -> {
            return new RoomSimpleViewResponseDto(
                    room.getId(),
                    room.getName(),
                    room.getStatus(),
                    counts.getOrDefault(room.getId(), 0L).intValue(),
                    Room.MAX_PLAYERS,
                    room.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public Integer getCurrentPlayer() {
        return currentPlayer;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
