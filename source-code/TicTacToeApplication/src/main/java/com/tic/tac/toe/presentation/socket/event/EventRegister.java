package com.tic.tac.toe.presentation.socket.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tic.tac.toe.AppContext;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.domain.event.*;
import com.tic.tac.toe.presentation.socket.event.handlers.*;

public final class EventRegister {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private EventRegister() {
    }

    public static EventDispatcher buildDispatcher(AppContext context) {
        EventDispatcher eventDispatcher = new EventDispatcher();
        objectMapper.registerModule(new JavaTimeModule());

        eventDispatcher.register(
                GlobalMessageEvent.class,
                new GlobalMessageHandler(
                        context.connectionManager,
                        context.userService,
                        objectMapper
                )
        );

        eventDispatcher.register(
                RoomsByStatusEvent.class,
                new RoomsByStatusHandler(
                        context.connectionManager,
                        context.roomPlayerService,
                        context.roomService,
                        objectMapper
                )
        );

        eventDispatcher.register(
                CreateRoomEvent.class,
                new CreateRoomHandler(
                        eventDispatcher,
                        context.connectionManager,
                        context.roomManager,
                        context.roomService,
                        objectMapper
                )
        );

        eventDispatcher.register(
                RoomMessageEvent.class,
                new MessageRoomHandler(
                        context.connectionManager,
                        context.roomManager,
                        context.userService,
                        objectMapper
                )
        );

        eventDispatcher.register(
                JoinRoomEvent.class,
                new JoinRoomHandler(
                        eventDispatcher,
                        context.connectionManager,
                        context.roomManager,
                        context.roomService,
                        context.userService,
                        context.roomPlayerService
                )
        );

        eventDispatcher.register(
                InfoRoomEvent.class,
                new InfoRoomHandler(
                        context.roomManager,
                        context.roomService,
                        objectMapper
                )
        );

        eventDispatcher.register(
                LeaveRoomEvent.class,
                new LeaveRoomHandler(
                        context.connectionManager,
                        context.roomManager,
                        context.roomService,
                        context.roomPlayerService,
                        eventDispatcher,
                        objectMapper
                )
        );

        eventDispatcher.register(
                ReadyRoomEvent.class,
                new ReadyRoomHandler(
                        context.roomPlayerService,
                        eventDispatcher
                )
        );

        eventDispatcher.register(
                StartGameEvent.class,
                new StartGameRoomHandler(
                        context.connectionManager,
                        context.roomManager,
                        context.gameService,
                        eventDispatcher,
                        objectMapper
                )
        );

        eventDispatcher.register(
                PlayGameEvent.class,
                new PlayGameHandler(
                        context.connectionManager,
                        context.roomManager,
                        context.gameService,
                        context.roomService,
                        eventDispatcher,
                        objectMapper
                )
        );

        return eventDispatcher;
    }
}
