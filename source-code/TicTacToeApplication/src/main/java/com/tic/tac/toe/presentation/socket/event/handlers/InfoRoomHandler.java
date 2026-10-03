package com.tic.tac.toe.presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.dto.message.InfoRoomDeliver;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import com.tic.tac.toe.domain.service.RoomService;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.message.SocketMessageDeliver;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InfoRoomHandler implements EventHandler<InfoRoomEvent> {
    private static final Logger log = LoggerFactory.getLogger(InfoRoomHandler.class);
    private final RoomManager roomManager;
    private final RoomService roomService;
    private final ObjectMapper objectMapper;

    public InfoRoomHandler(
            RoomManager roomManager,
            RoomService roomService,
            ObjectMapper objectMapper
    ) {
        this.roomManager = roomManager;
        this.roomService = roomService;
        this.objectMapper = objectMapper;
        log.info("Instance {} initialized. [InstanceId={}]",
                InfoRoomHandler.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public void handle(InfoRoomEvent event) {
        Room room = roomService.findById(event.getRoomId());
        if (room == null) {
            log.warn("Room not found. [roomId={}]", event.getEvent());
            throw new RuntimeException("Room not found.");
        }
        InfoRoomDeliver deliver = InfoRoomDeliver.of(room);
        SocketMessageDeliver socketDeliver = new SocketMessageDeliver(
                event.getEvent(),
                objectMapper.valueToTree(deliver)
        );
        try {
            String objectJson = objectMapper.writeValueAsString(socketDeliver);
            roomManager.broadcast(event.getRoomId(), objectJson);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Error on transform SocketMessageDeliver in json. [event={}]", event);
            throw new RuntimeException("Error on transform SocketMessageDeliver in json.");
        }
    }
}
