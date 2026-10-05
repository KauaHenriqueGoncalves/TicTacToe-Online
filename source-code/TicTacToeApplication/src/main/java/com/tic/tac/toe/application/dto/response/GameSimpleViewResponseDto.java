package com.tic.tac.toe.application.dto.response;

import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.enums.Mark;
import java.util.UUID;

public final class GameSimpleViewResponseDto {
    private final UUID id;
    private final String board;
    private final UUID xPlayerId;
    private final UUID oPlayerId;
    private final Mark turn;

    public GameSimpleViewResponseDto(UUID id, String board, UUID xPlayerId, UUID oPlayerId, Mark turn) {
        this.id = id;
        this.board = board;
        this.xPlayerId = xPlayerId;
        this.oPlayerId = oPlayerId;
        this.turn = turn;
    }

    public static GameSimpleViewResponseDto of(Game game) {
        if (game == null) return null;

        return new GameSimpleViewResponseDto(
                game.getId(),
                game.getBoard(),
                game.getXPlayerId(),
                game.getOPlayerId(),
                game.getTurn()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getBoard() {
        return board;
    }

    public UUID getXPlayerId() {
        return xPlayerId;
    }

    public UUID getOPlayerId() {
        return oPlayerId;
    }

    public Mark getTurn() {
        return turn;
    }
}
