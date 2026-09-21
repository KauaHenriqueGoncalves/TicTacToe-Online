package com.tic.tac.toe.presentation.socket.message;

import com.fasterxml.jackson.databind.JsonNode;

public final class SocketMessageReceive {
    private String event;
    private String accessToken;
    private JsonNode content;

    public SocketMessageReceive() {
    }

    public SocketMessageReceive(String event, String accessToken, JsonNode content) {
        this.event = event;
        this.accessToken = accessToken;
        this.content = content;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }
}
