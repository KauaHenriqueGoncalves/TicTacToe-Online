package com.tic.tac.toe.application.dto.event.deliver;

public final class GlobalMessageDeliver {
    private final String from;
    private final String message;

    public GlobalMessageDeliver(String from, String message) {
        this.from = from;
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }
}
