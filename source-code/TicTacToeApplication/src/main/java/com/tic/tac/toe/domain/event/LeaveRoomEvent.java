package com.tic.tac.toe.domain.event;

public final class LeaveRoomEvent implements DomainEvent {
    private final String roomId;
    private final String userId;

    public LeaveRoomEvent(String roomId, String userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

    @Override
    public String getEvent() {
        return "room.leave";
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUserId() {
        return userId;
    }
}
