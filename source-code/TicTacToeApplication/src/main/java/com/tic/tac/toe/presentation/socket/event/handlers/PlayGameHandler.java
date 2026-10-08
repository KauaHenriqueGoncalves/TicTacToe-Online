package com.tic.tac.toe.presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.dto.message.GameDrawDeliver;
import com.tic.tac.toe.application.dto.message.GameWinnerDeliver;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.application.event.EventPublisher;
import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.enums.Mark;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.DomainEvent;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import com.tic.tac.toe.domain.event.PlayGameEvent;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.service.GameService;
import com.tic.tac.toe.domain.service.RoomService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayGameHandler implements EventHandler<PlayGameEvent> {
    private static final Logger log = LoggerFactory.getLogger(PlayGameHandler.class);
    private final ConnectionManager connectionManager;
    private final RoomManager roomManager;
    private final GameService gameService;
    private final RoomService roomService;
    private final EventPublisher publisher;
    private final ObjectMapper objectMapper;

    public PlayGameHandler(
            ConnectionManager connectionManager,
            RoomManager roomManager,
            GameService gameService,
            RoomService roomService,
            EventPublisher publisher,
            ObjectMapper objectMapper
    ) {
        this.connectionManager = connectionManager;
        this.roomManager = roomManager;
        this.gameService = gameService;
        this.roomService = roomService;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        log.info("Instance {} initialized. [InstanceId={}]",
                PlayGameHandler.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public void handle(PlayGameEvent event) {
        UUID roomId = event.getRoomId();
        UUID userId = event.getUserId();
        Connection connection = connectionManager.getByUserId(userId);
        if (connection == null) {
            throw new UnauthorizedException("Connection not found for user");
        }
        if (!roomId.equals(connection.getRoomId())) {
            throw new InputInvalidException("User is not in this room");
        }
        Game game;
        try {
            game = gameService.play(roomId, userId, event.getPosition());
        } catch (OptimisticLockException e) {
            throw new InputInvalidException("Concurrent move, try again");
        }
        publishSafely(new InfoRoomEvent(roomId, userId));
        Mark winner = game.winner();
        if (winner != null) {
            UUID winnerId = winner == Mark.X ? game.getXPlayerId() : game.getOPlayerId();
            String username = game.getRoom().getWinnerName();
            send(roomId, "room.game.winner", new GameWinnerDeliver(username, winnerId.toString()));
            log.info("Game finished. [roomId={}] [winner={}]", roomId, winnerId);
            finishRoom(roomId, userId);
        } else if (game.isDraw()) {
            send(roomId, "room.game.draw", new GameDrawDeliver("Empate"));
        }
    }

    private void send(UUID roomId, String event, Object content) {
        try {
            String json = objectMapper.writeValueAsString(
                    new SocketMessageDeliver(event, objectMapper.valueToTree(content)));
            roomManager.broadcast(roomId, json);
        } catch (JsonProcessingException e) {
            log.error("Error serializing message. [event={}] [error={}]", event, e.getMessage());
        }
    }

    private void publishSafely(DomainEvent e) {
        try {
            publisher.publish(e);
        } catch (RuntimeException ex) {
            log.error("Error publishing event. [event={}] [error={}]", e.getEvent(), ex.getMessage());
        }
    }

    private void finishRoom(UUID roomId, UUID userId) {
        send(roomId, "room.kicked", new GameDrawDeliver("Partida encerrada"));

        try {
            List<UUID> userIds = new ArrayList<>(roomManager.get(roomId).getUsers().keySet());
            log.info("Kicking players. [roomId={}] [count={}]", roomId, userIds.size());
            for (UUID uid : userIds) {
                Connection c = connectionManager.getByUserId(uid);
                if (c != null) {
                    roomManager.leave(roomId, c);
                }
            }
            roomManager.removeById(roomId);
        } catch (RuntimeException e) {
            log.error("Error kicking players. [roomId={}] [error={}]", roomId, e.getMessage());
        }

        try {
            roomService.delete(roomId);
        } catch (RuntimeException e) {
            log.error("Error deleting finished room. [roomId={}] [error={}]", roomId, e.getMessage());
        }

        publishSafely(new RoomsByStatusEvent(RoomStatus.WAITING, userId, true));
    }
}
