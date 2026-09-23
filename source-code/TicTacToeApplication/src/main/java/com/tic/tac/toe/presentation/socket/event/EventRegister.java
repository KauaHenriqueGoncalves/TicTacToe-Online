package com.tic.tac.toe.presentation.socket.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tic.tac.toe.AppContext;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.domain.event.GlobalMessageEvent;
import com.tic.tac.toe.presentation.socket.event.handlers.GlobalMessageHandler;
import com.tic.tac.toe.presentation.socket.event.handlers.MessageHandler;
import com.tic.tac.toe.domain.event.MessageEvent;

public final class EventRegister {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private EventRegister() {
    }

    public static EventDispatcher buildDispatcher(AppContext context) {
        EventDispatcher eventDispatcher = new EventDispatcher();
        objectMapper.registerModule(new JavaTimeModule());

        eventDispatcher.register(
                MessageEvent.class,
                new MessageHandler(context.connectionManager)
        );

        eventDispatcher.register(
                GlobalMessageEvent.class,
                new GlobalMessageHandler(context.connectionManager, context.userService, objectMapper)
        );

        return eventDispatcher;
    }
}
