package com.tic.tac.toe.application.dto.message;

import com.tic.tac.toe.application.dto.response.GameSimpleViewResponseDto;
import com.tic.tac.toe.application.dto.response.RoomPlayerInfoRoomResponseDto;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
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
    private final GameSimpleViewResponseDto game;
    private final List<RoomPlayerInfoRoomResponseDto> players;

    public InfoRoomDeliver(UUID id, String name, UUID ownerId, RoomStatus status, UUID currentPlayer, UUID winnerId, String winnerName, GameSimpleViewResponseDto game, List<RoomPlayerInfoRoomResponseDto> players) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.status = status;
        this.currentPlayer = currentPlayer;
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.game = game;
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
                GameSimpleViewResponseDto.of(room.getGame()),
                room.getPlayers().stream().map(p -> {
                    return new RoomPlayerInfoRoomResponseDto(
                            p.getId(),
                            p.getUser().getId(),
                            p.getUser().getUsername(),
                            p.getPlaying(),
                            p.getReady()
                    );
                }).collect(Collectors.toList()));
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

    public GameSimpleViewResponseDto getGame() {
        return game;
    }

    public List<RoomPlayerInfoRoomResponseDto> getPlayers() {
        return players;
    }
}
