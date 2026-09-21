package com.tic.tac.toe.presentation.socket.message;

import com.fasterxml.jackson.databind.JsonNode;

public final class SocketMessageDeliver {
    private String event;
    private JsonNode content;

    public SocketMessageDeliver() {
    }

    public SocketMessageDeliver(String event, JsonNode content) {
        this.event = event;
        this.content = content;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }
}
