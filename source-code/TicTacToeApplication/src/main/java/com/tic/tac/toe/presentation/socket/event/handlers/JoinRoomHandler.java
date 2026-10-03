package com.tic.tac.toe.presentation.socket.event.handlers;

import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.event.JoinRoomEvent;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.service.RoomPlayerService;
import com.tic.tac.toe.domain.service.RoomService;
import com.tic.tac.toe.domain.service.UserService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JoinRoomHandler implements EventHandler<JoinRoomEvent> {
    private static final Logger log = LoggerFactory.getLogger(JoinRoomHandler.class);
    private final EventDispatcher eventDispatcher;
    private final ConnectionManager connectionManager;
    private final RoomManager roomManager;
    private final RoomService roomService;
    private final UserService userService;
    private final RoomPlayerService roomPlayerService;

    public JoinRoomHandler(
            EventDispatcher eventDispatcher,
            ConnectionManager connectionManager,
            RoomManager roomManager,
            RoomService roomService,
            UserService userService,
            RoomPlayerService roomPlayerService
    ) {
        this.eventDispatcher = eventDispatcher;
        this.connectionManager = connectionManager;
        this.roomManager = roomManager;
        this.roomService = roomService;
        this.userService = userService;
        this.roomPlayerService = roomPlayerService;
        log.info("Instance {} initialized. [InstanceId={}]",
                JoinRoomHandler.class.getSimpleName(), System.identityHashCode(this));
    }


    @Override
    public void handle(JoinRoomEvent event) {
        Connection connection = connectionManager.getByUserId(event.getUserId());
        if (connection == null) {
            log.warn("User has no active connection. [userId={}]", event.getUserId());
            throw new RuntimeException("Connection not found for user");
        }
        if (connection.getRoomId() != null) {
            log.warn("User is already in a room. [userId={}] [roomId={}]",
                    event.getUserId(), connection.getRoomId());
            throw new RuntimeException("User is already in a room.");
        }
        User user = userService.findById(event.getUserId());
        Room room = roomService.findById(event.getRoomId());
        if (room.getPlayers().size() >= Room.MAX_PLAYERS) {
            log.warn("Room is full. [roomId={}]", room.getId());
            throw new RuntimeException("Room is full.");
        }
        RoomPlayer player = roomPlayerService.create(RoomPlayer.init(room, user));
        roomManager.join(room.getId(), connection);
        eventDispatcher.publish(new RoomsByStatusEvent(RoomStatus.WAITING, connection.getUserId(), true));
        // evento de atualizar estado atual da sala
    }
}
