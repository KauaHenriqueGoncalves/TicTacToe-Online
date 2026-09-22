package com.tic.tac.toe.application.event;

import com.tic.tac.toe.domain.event.DomainEvent;
import java.util.HashMap;
import java.util.Map;

public final class EventDispatcher {
    private final Map<String, EventHandler<?>> handlers = new HashMap<>();

    public <T extends DomainEvent> void register(String event, EventHandler<T> handler) {
        handlers.put(event, handler);
    }

    public void dispatch(DomainEvent event) {
        EventHandler handler = handlers.get(event.getEvent());
        if (handler == null) {
            throw new IllegalArgumentException(
                    "No handler registered for event: " + event.getEvent()
            );
        }
        handler.handle(event);
    }
}
