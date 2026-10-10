package com.tic.tac.toe.application.service;

import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.enums.Mark;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.repositoy.GameRepository;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import com.tic.tac.toe.domain.repositoy.RoomRepository;
import com.tic.tac.toe.domain.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class GameServiceImpl implements GameService {
    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);
    private final GameRepository gameRepository;
    private final RoomRepository roomRepository;
    private final RoomPlayerRepository roomPlayerRepository;

    public GameServiceImpl(
            GameRepository gameRepository,
            RoomRepository roomRepository,
            RoomPlayerRepository roomPlayerRepository
    ) {
        this.gameRepository = gameRepository;
        this.roomRepository = roomRepository;
        this.roomPlayerRepository = roomPlayerRepository;
    }

    @Override
    public Game start(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        if (gameRepository.findByRoomId(roomId).isPresent()) {
            throw new InputInvalidException("Game already started");
        }
        List<RoomPlayer> players = roomPlayerRepository.findByRoomId(roomId);
        if (players.size() != Room.MAX_PLAYERS) {
            throw new InputInvalidException("Room needs 2 players");
        }
        if (!players.stream().allMatch(RoomPlayer::getReady)) {
            throw new InputInvalidException("All players must be ready");
        }
        Collections.shuffle(players);
        UUID x = players.get(0).getUser().getId();
        UUID o = players.get(1).getUser().getId();
        Game game = gameRepository.save(Game.init(room, x, o));
        room.setStatus(RoomStatus.IN_GAME);
        room.setCurrentPlayerId(x);
        roomRepository.save(room);
        log.info("Game started. [roomId={}] [x={}] [o={}]", roomId, x, o);
        return game;
    }

    @Override
    public Game findByRoomId(UUID roomId) {
        return gameRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
    }

    @Override
    public Game play(UUID roomId, UUID userId, int position) {
        Game game = findByRoomId(roomId);
        if (game.winner() != null || game.isDraw()) {
            throw new InputInvalidException("Game already finished");
        }
        game.play(userId, position);
        Room room = game.getRoom();
        Mark winner = game.winner();
        if (winner != null) {
            UUID winnerId = winner == Mark.X ? game.getXPlayerId() : game.getOPlayerId();
            room.setWinnerId(winnerId);
            room.setWinnerName(userNameOf(roomId, winnerId));
            room.setStatus(RoomStatus.FINISHED);

        // remover todo mundo a sala pelo roommanager

        } else if (game.isDraw()) {
            room.setStatus(RoomStatus.FINISHED);
        } else {
            room.setCurrentPlayerId(game.getTurn() == Mark.X ? game.getXPlayerId() : game.getOPlayerId());
        }
        return gameRepository.save(game);
    }

    private String userNameOf(UUID roomId, UUID userId) {
        return roomPlayerRepository.findByRoomId(roomId).stream()
                .filter(rp -> rp.getUser().getId().equals(userId))
                .map(rp -> rp.getUser().getUsername())
                .findFirst().orElse(null);
    }
}
