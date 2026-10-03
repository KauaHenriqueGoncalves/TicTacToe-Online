package com.tic.tac.toe.domain.event;

public final class InfoRoomEvent implements DomainEvent {
    private final String roomId;

    public InfoRoomEvent(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String getEvent() {
        return "room.info";
    }

    public String getRoomId() {
        return roomId;
    }
}
