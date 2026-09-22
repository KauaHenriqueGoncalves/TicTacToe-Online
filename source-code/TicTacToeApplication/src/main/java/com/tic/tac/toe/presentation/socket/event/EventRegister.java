package com.tic.tac.toe.presentation.socket.event;

import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.application.event.handlers.MessageHandler;
import com.tic.tac.toe.domain.event.MessageEvent;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;

public final class EventRegister {
    public static EventDispatcher buildDispatcher() {
        EventDispatcher eventDispatcher = new EventDispatcher();

        eventDispatcher.register(
                MessageEvent.class,
                new MessageHandler(ConnectionManager.getFactory())
        );

        return eventDispatcher;
    }

}
