package com.tic.tac.toe.application.dto.message;

import com.tic.tac.toe.application.dto.response.RoomPlayerInfoRoomResponseDto;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class InfoRoomDeliver {
    private final UUID id;
    private final String name;
    private final UUID ownerId;
    private final RoomStatus status;
    private final UUID currentPlayer;
    private final UUID winnerId;
    private final String winnerName;
    private final List<RoomPlayerInfoRoomResponseDto> players;

    public InfoRoomDeliver(UUID id, String name, UUID ownerId, RoomStatus status, UUID currentPlayer, UUID winnerId, String winnerName, List<RoomPlayerInfoRoomResponseDto> players) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.status = status;
        this.currentPlayer = currentPlayer;
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.players = players;
    }

    public static InfoRoomDeliver of(Room room) {
        return new InfoRoomDeliver(
                room.getId(),
                room.getName(),
                room.getOwnerId(),
                room.getStatus(),
                room.getCurrentPlayerId(),
                room.getWinnerId(),
                room.getWinnerName(),
                room.getPlayers().stream().map(p -> {
                    return new RoomPlayerInfoRoomResponseDto(
                            p.getId(),
                            p.getUser().getId(),
                            p.getUser().getUsername(),
                            p.getPlaying(),
                            p.getReady()
                    );
                }).collect(Collectors.toList())
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public UUID getCurrentPlayer() {
        return currentPlayer;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public List<RoomPlayerInfoRoomResponseDto> getPlayers() {
        return players;
    }
}
