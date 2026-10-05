package com.tic.tac.toe.presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.dto.message.MessageRoomDeliver;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.event.RoomMessageEvent;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.service.UserService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public final class MessageRoomHandler implements EventHandler<RoomMessageEvent> {
    private static final Logger log = LoggerFactory.getLogger(MessageRoomHandler.class);
    private final ConnectionManager connectionManager;
    private final RoomManager roomManager;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public MessageRoomHandler(
            ConnectionManager connectionManager,
            RoomManager roomManager,
            UserService userService,
            ObjectMapper objectMapper
    ) {
        this.connectionManager = connectionManager;
        this.roomManager = roomManager;
        this.userService = userService;
        this.objectMapper = objectMapper;
        log.info("Instance {} initialized. [InstanceId={}]",
                MessageRoomHandler.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public void handle(RoomMessageEvent event) {
        Connection connection = connectionManager.getByUserId(event.getUserId());
        if (connection == null) {
            log.warn("User has no active connection. [userId={}]", event.getUserId());
            throw new UnauthorizedException("Connection not found for user");
        }
        if (connection.getRoomId() == null) {
            log.warn("User don't have a room. [userId={}]",
                    event.getUserId());
            throw new InputInvalidException("User don't have a room.");
        }
        User user = userService.findById(event.getUserId());
        MessageRoomDeliver deliver = new MessageRoomDeliver(
                user.getUsername(),
                event.getMessage(),
                LocalDateTime.now()
        );
        SocketMessageDeliver deliverSocket = new SocketMessageDeliver(
                event.getEvent(),
                objectMapper.valueToTree(deliver)
        );
        try {
            String objectJson = objectMapper.writeValueAsString(deliverSocket);
            roomManager.broadcast(connection.getRoomId(), objectJson);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Error on transform SocketMessageDeliver in json. [event={}]", event);
            throw new RuntimeException("Error on transform SocketMessageDeliver in json.");
        }
    }
}
