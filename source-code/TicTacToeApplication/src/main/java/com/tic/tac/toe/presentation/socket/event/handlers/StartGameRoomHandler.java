package com.tic.tac.toe.presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.dto.message.GameStartedDeliver;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.application.event.EventPublisher;
import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.event.StartGameEvent;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.service.GameService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class StartGameRoomHandler implements EventHandler<StartGameEvent> {
    private static final Logger log = LoggerFactory.getLogger(StartGameRoomHandler.class);
    private final ConnectionManager connectionManager;
    private final RoomManager roomManager;
    private final GameService gameService;
    private final EventPublisher publisher;
    private final ObjectMapper objectMapper;

    public StartGameRoomHandler(
            ConnectionManager connectionManager,
            RoomManager roomManager,
            GameService gameService,
            EventPublisher publisher,
            ObjectMapper objectMapper
    ) {
        this.connectionManager = connectionManager;
        this.roomManager = roomManager;
        this.gameService = gameService;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        log.info("Instance {} initialized. [InstanceId={}]",
                StartGameRoomHandler.class.getSimpleName(), System.identityHashCode(this));
    }


    @Override
    public void handle(StartGameEvent event) {
        UUID roomId = event.getRoomId();
        UUID userId = event.getUserId();

        Connection connection = connectionManager.getByUserId(userId);
        if (connection == null) {
            throw new UnauthorizedException("Connection not found for user");
        }
        if (!roomId.equals(connection.getRoomId())) {
            throw new InputInvalidException("User is not in this room");
        }

        // valida 2 jogadores + todos ready e cria o Game
        Game game = gameService.start(roomId);

        GameStartedDeliver deliver = new GameStartedDeliver(
                game.getBoard(),
                game.getXPlayerId().toString(),
                game.getOPlayerId().toString(),
                game.getTurn().name());

        try {
            String json = objectMapper.writeValueAsString(new SocketMessageDeliver(
                    event.getEvent(), objectMapper.valueToTree(deliver)));
            roomManager.broadcast(roomId, json);
        } catch (JsonProcessingException e) {
            log.error("Error serializing game start. [error={}]", e.getMessage());
            throw new RuntimeException("Error serializing game start.");
        }

        log.info("Game started. [roomId={}] [by={}]", roomId, userId);

        try {
            publisher.publish(new InfoRoomEvent(roomId, userId));
            publisher.publish(new RoomsByStatusEvent(RoomStatus.WAITING, userId, true));
        } catch (RuntimeException e) {
            log.error("Error publishing rooms list. [error={}]", e.getMessage());
        }
    }
}
