package com.tic.tac.toe.presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.dto.message.LeaveRoomSuccessfulDeliver;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.application.event.EventPublisher;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.DomainEvent;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import com.tic.tac.toe.domain.event.LeaveRoomEvent;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.service.RoomPlayerService;
import com.tic.tac.toe.domain.service.RoomService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public final class LeaveRoomHandler implements EventHandler<LeaveRoomEvent> {
    private static final Logger log = LoggerFactory.getLogger(LeaveRoomHandler.class);
    private final ConnectionManager connectionManager;
    private final RoomManager roomManager;
    private final RoomService roomService;
    private final RoomPlayerService roomPlayerService;
    private final EventPublisher publisher;
    private final ObjectMapper objectMapper;

    public LeaveRoomHandler(
            ConnectionManager connectionManager,
            RoomManager roomManager,
            RoomService roomService,
            RoomPlayerService roomPlayerService,
            EventPublisher publisher,
            ObjectMapper objectMapper
    ) {
        this.connectionManager = connectionManager;
        this.roomManager = roomManager;
        this.roomService = roomService;
        this.roomPlayerService = roomPlayerService;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        log.info("Instance {} initialized. [InstanceId={}]",
                LeaveRoomHandler.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public void handle(LeaveRoomEvent event) {
        UUID roomId = parse(event.getRoomId());
        UUID userId = parse(event.getUserId());
        Connection connection = connectionManager.getByUserId(userId);
        if (connection == null) {
            throw new UnauthorizedException("Connection not found for user");
        }
        if (!roomId.equals(connection.getRoomId())) {
            throw new InputInvalidException("User is not in this room");
        }
        roomManager.leave(roomId, connection);
        try {
            roomPlayerService.deleteByRoomIdAndUserId(roomId, userId);
        } catch (NotFoundException e) {
            log.warn("RoomPlayer already removed. [roomId={}] [userId={}]", roomId, userId);
        }
        send(connection, event.getEvent(), new LeaveRoomSuccessfulDeliver("Você saiu da sala"));
        boolean roomDeleted = false;
        try {
            if (roomManager.get(roomId).getUsers().isEmpty()) {
                roomManager.removeById(roomId);
                roomService.delete(roomId);
                roomDeleted = true;
            }
        } catch (RuntimeException e) {
            log.error("Error removing empty room. [roomId={}] [error={}]", roomId, e.getMessage());
        }
        publishSafely(new RoomsByStatusEvent(RoomStatus.WAITING, userId, true));
        if (!roomDeleted) {
            publishSafely(new InfoRoomEvent(roomId, userId));
        }
    }

    private void send(Connection c, String event, Object content) {
        try {
            String json = objectMapper.writeValueAsString(
                    new SocketMessageDeliver(event, objectMapper.valueToTree(content)));
            if (c.getConnection().isOpen()) c.getConnection().send(json);
        } catch (JsonProcessingException e) {
            log.error("Error serializing message. [event={}]", event);
        }
    }

    private void publishSafely(DomainEvent e) {
        try {
            publisher.publish(e);
        } catch (RuntimeException ex) {
            log.error("Error publishing event. [event={}] [error={}]", e.getEvent(), ex.getMessage());
        }
    }

    private UUID parse(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InputInvalidException("Invalid id");
        }
    }
}
