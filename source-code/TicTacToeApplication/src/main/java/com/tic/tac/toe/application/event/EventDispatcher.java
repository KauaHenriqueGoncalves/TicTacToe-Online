package com.tic.tac.toe.application.event;

import com.tic.tac.toe.domain.event.DomainEvent;
import java.util.HashMap;
import java.util.Map;

public final class EventDispatcher {
    private final Map<
            Class<? extends DomainEvent>,
            EventHandler<? extends DomainEvent>
    > handlers = new HashMap<>();

    public <T extends DomainEvent> void register(Class<T> eventType, EventHandler<T> handler) {
        handlers.put(eventType, handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void dispatch(T event) {
        EventHandler<T> handler = (EventHandler<T>) handlers.get(event.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler for event: " + event.getClass().getSimpleName());
        }
        handler.handle(event);
    }
}
