package com.tic.tac.toe.presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.dto.message.RoomsSimpleViewDeliver;
import com.tic.tac.toe.application.dto.response.RoomSimpleViewResponseDto;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.service.RoomPlayerService;
import com.tic.tac.toe.domain.service.RoomService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public final class RoomsByStatusHandler implements EventHandler<RoomsByStatusEvent> {
    private static final Logger log = LoggerFactory.getLogger(RoomsByStatusHandler.class);
    private final ConnectionManager connectionManager;
    private final RoomPlayerService roomPlayerService;
    private final RoomService roomService;
    private final ObjectMapper objectMapper;

    public RoomsByStatusHandler(
            ConnectionManager connectionManager,
            RoomPlayerService roomPlayerService,
            RoomService roomService,
            ObjectMapper objectMapper
    ) {
        this.connectionManager = connectionManager;
        this.roomPlayerService = roomPlayerService;
        this.roomService = roomService;
        this.objectMapper = objectMapper;
        log.info("Instance {} initialized. [InstanceId={}]",
                RoomsByStatusHandler.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public void handle(RoomsByStatusEvent event) {
        if (!RoomStatus.constains(event.getStatus())) {
            log.warn("Invalid status to find.");
            throw new IllegalArgumentException("Invalid status");
        }
        List<Room> rooms = roomService.findAllByStatus(event.getStatus().getStatus());
        List<RoomSimpleViewResponseDto> simpleView = RoomSimpleViewResponseDto.of(rooms, roomPlayerService.countGroupedByRoomId());
        RoomsSimpleViewDeliver deliver = new RoomsSimpleViewDeliver(simpleView);
        SocketMessageDeliver deliverSocket = new SocketMessageDeliver(event.getEvent(), objectMapper.valueToTree(deliver));
        try {
            String objectJson = objectMapper.writeValueAsString(deliverSocket);
            if (event.isBroadcast()) {
                connectionManager.broadcast(objectJson);
                return;
            }
            Connection connection = connectionManager.getByUserId(event.getUserId());
            if (connection == null) {
                log.warn("Owner has no active connection. [ownerId={}]", event.getUserId());
                throw new UnauthorizedException("Connection not found for user");
            }
            connection.getConnection().send(objectJson);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Error on transform SocketMessageDeliver in json. [event={}]", event);
            throw new RuntimeException("Error on transform SocketMessageDeliver in json.");
        }
    }
}
