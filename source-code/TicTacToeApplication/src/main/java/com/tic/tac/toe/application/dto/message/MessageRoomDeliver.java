package com.tic.tac.toe.application.dto.message;

import java.time.LocalDateTime;

public final class MessageRoomDeliver {
    private final String from;
    private final String message;
    private final LocalDateTime sentAt;

    public MessageRoomDeliver(String from, String message, LocalDateTime sentAt) {
        this.from = from;
        this.message = message;
        this.sentAt = sentAt;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
