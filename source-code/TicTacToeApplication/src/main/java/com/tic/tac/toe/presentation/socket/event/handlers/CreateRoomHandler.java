package com.tic.tac.toe.presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.dto.message.CreateRoomSuccessfulDeliver;
import com.tic.tac.toe.application.dto.request.CreateRoomRequestDto;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.CreateRoomEvent;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.service.RoomService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreateRoomHandler implements EventHandler<CreateRoomEvent> {
    private static final Logger log = LoggerFactory.getLogger(CreateRoomHandler.class);
    private final EventDispatcher eventDispatcher;
    private final ConnectionManager connectionManager;
    private final RoomManager roomManager;
    private final RoomService roomService;
    private final ObjectMapper objectMapper;

    public CreateRoomHandler(
            EventDispatcher eventDispatcher,
            ConnectionManager connectionManager,
            RoomManager roomManager,
            RoomService roomService,
            ObjectMapper objectMapper
    ) {
        this.eventDispatcher = eventDispatcher;
        this.connectionManager = connectionManager;
        this.roomManager = roomManager;
        this.roomService = roomService;
        this.objectMapper = objectMapper;
        log.info("Instance {} initialized. [InstanceId={}]",
                CreateRoomHandler.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public void handle(CreateRoomEvent event) {
        Connection connection = connectionManager.getByUserId(event.getOwnerId());
        if (connection == null) {
            log.warn("Owner has no active connection. [ownerId={}]", event.getOwnerId());
            throw new UnauthorizedException("Connection not found for user");
        }
        if (connection.getRoomId() != null) {
            log.warn("User is already in a room. [userId={}] [roomId={}]",
                    event.getOwnerId(), connection.getRoomId());
            throw new InputInvalidException("User is already in a room");
        }
        Room room = roomService.create(
                new CreateRoomRequestDto(event.getName(), event.getOwnerId()));
        try {
            roomManager.create(room.getId());
            roomManager.join(room.getId(), connection);
            roomManager.broadcast(room.getId(), toJson(event.getEvent(),
                    new CreateRoomSuccessfulDeliver("Sala criada com sucesso")));
            eventDispatcher.publish(new RoomsByStatusEvent(RoomStatus.WAITING, connection.getUserId(), true));
            eventDispatcher.publish(new InfoRoomEvent(room.getId(), event.getOwnerId()));
            log.info("Room created and owner joined. [roomId={}] [ownerId={}]",
                    room.getId(), event.getOwnerId());
        } catch (RuntimeException e) {
            log.error("Error setting up room in memory, rolling back. [roomId={}] [error={}]",
                    room.getId(), e.getMessage());
            roomManager.leave(room.getId(), connection);
            roomManager.removeById(room.getId());
            roomService.delete(room.getId());
            throw e;
        }
    }

    private String toJson(String event, Object content) {
        try {
            return objectMapper.writeValueAsString(
                    new SocketMessageDeliver(event, objectMapper.valueToTree(content)));
        } catch (JsonProcessingException e) {
            log.error("Error on transform SocketMessageDeliver in json. [event={}]", event);
            throw new RuntimeException("Error on transform SocketMessageDeliver in json.");
        }
    }
}
