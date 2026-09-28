package com.tic.tac.toe.domain.entity.enums;

public enum RoomStatus {
    WAITING("waiting"),
    IN_GAME("in_game"),
    FINISHED("finished");

    private final String status;

    RoomStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean isEqual(RoomStatus status) {
        for (RoomStatus roomStatus : RoomStatus.values()) {
            if (roomStatus.getStatus().equals(status.getStatus())) {
                return true;
            }
        }
        return false;
    }
}
