package com.tic.tac.toe.presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.dto.message.GlobalMessageDeliver;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.event.GlobalMessageEvent;
import com.tic.tac.toe.domain.service.UserService;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GlobalMessageHandler implements EventHandler<GlobalMessageEvent> {
    private static final Logger log = LoggerFactory.getLogger(GlobalMessageHandler.class);
    private final ConnectionManager connectionManager;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public GlobalMessageHandler(ConnectionManager connectionManager, UserService userService, ObjectMapper objectMapper) {
        this.connectionManager = connectionManager;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(GlobalMessageEvent event) {
        User user = userService.findById(event.getUserId());
        GlobalMessageDeliver content = new GlobalMessageDeliver(user.getUsername(), event.getMessage());
        SocketMessageDeliver deliver = new SocketMessageDeliver(event.getEvent(), objectMapper.valueToTree(content));
        try {
            connectionManager.broadcast(objectMapper.writeValueAsString(deliver));
        } catch (JsonProcessingException e) {
            log.error("Error on transform GlobalMessageDeliver in json.");
            throw new RuntimeException("Error on transform GlobalMessageDeliver in json.");
        }
    }
}
