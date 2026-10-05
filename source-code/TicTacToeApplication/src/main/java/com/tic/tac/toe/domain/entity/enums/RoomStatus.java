package com.tic.tac.toe.domain.entity.enums;

public enum RoomStatus {
    WAITING("WAITING"),
    IN_GAME("IN_GAME"),
    FINISHED("FINISHED");

    private final String status;

    RoomStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static RoomStatus fromString(String status) {
        for (RoomStatus roomStatus : RoomStatus.values()) {
            if (roomStatus.getStatus().equals(status)) {
                return roomStatus;
            }
        }
        return null;
    }

    public static boolean constains(RoomStatus status) {
        for (RoomStatus roomStatus : RoomStatus.values()) {
            if (roomStatus.getStatus().equals(status.getStatus())) {
                return true;
            }
        }
        return false;
    }
}
