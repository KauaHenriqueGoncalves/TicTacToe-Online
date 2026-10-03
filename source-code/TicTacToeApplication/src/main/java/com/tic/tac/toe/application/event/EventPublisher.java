package com.tic.tac.toe.application.event;

import com.tic.tac.toe.domain.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
