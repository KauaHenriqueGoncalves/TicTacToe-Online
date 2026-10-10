package com.tic.tac.toe.domain.event;

public final class ReadyRoomEvent implements DomainEvent {
    private final String roomId;
    private final String userId;

    public ReadyRoomEvent(String roomId, String userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

    @Override
    public String getEvent() {
        return "room.ready";
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUserId() {
        return userId;
    }
}
