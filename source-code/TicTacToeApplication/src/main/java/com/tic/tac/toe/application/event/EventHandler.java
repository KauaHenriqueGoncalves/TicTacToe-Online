package com.tic.tac.toe.application.event;

import com.tic.tac.toe.domain.event.DomainEvent;

public interface EventHandler<T extends DomainEvent> {
    void handle(T event);
}
