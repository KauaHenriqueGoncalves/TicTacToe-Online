package com.tic.tac.toe.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public final class GlobalMessageEvent implements DomainEvent {
    private final UUID userId;
    private final String message;
    private final LocalDateTime sentAt;

    public GlobalMessageEvent(UUID userId, String message) {
        this.userId = userId;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    @Override
    public String getEvent() {
        return "global.message";
    }

    public UUID getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
