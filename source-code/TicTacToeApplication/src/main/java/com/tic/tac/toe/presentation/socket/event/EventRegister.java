package com.tic.tac.toe.presentation.socket.event;

import com.tic.tac.toe.AppContext;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.application.event.handlers.MessageHandler;
import com.tic.tac.toe.domain.event.MessageEvent;

public final class EventRegister {
    public static EventDispatcher buildDispatcher(AppContext context) {
        EventDispatcher eventDispatcher = new EventDispatcher();

        eventDispatcher.register(
                MessageEvent.class,
                new MessageHandler(context.connectionManager)
        );

        return eventDispatcher;
    }

}
