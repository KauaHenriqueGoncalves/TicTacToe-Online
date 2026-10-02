package com.tic.tac.toe.presentation.socket.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tic.tac.toe.AppContext;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.domain.event.CreateRoomEvent;
import com.tic.tac.toe.domain.event.GlobalMessageEvent;
import com.tic.tac.toe.domain.event.RoomMessageEvent;
import com.tic.tac.toe.presentation.socket.event.handlers.CreateRoomHandler;
import com.tic.tac.toe.presentation.socket.event.handlers.GlobalMessageHandler;
import com.tic.tac.toe.presentation.socket.event.handlers.MessageRoomHandler;

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
                CreateRoomEvent.class,
                new CreateRoomHandler(
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

        return eventDispatcher;
    }
}
