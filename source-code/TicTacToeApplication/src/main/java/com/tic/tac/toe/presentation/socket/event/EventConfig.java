package com.tic.tac.toe.presentation.socket.event;

import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.application.event.handlers.MessageHandler;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;

public final class EventConfig {
    public static EventDispatcher register() {
        EventDispatcher eventDispatcher = new EventDispatcher();

        eventDispatcher.register(
                "message",
                new MessageHandler(ConnectionManager.getFactory())
        );

        return eventDispatcher;
    }
}
