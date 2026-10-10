package com.tic.tac.toe.application.dto.message;

import com.tic.tac.toe.application.dto.response.RoomSimpleViewResponseDto;
import java.util.List;

public final class RoomsSimpleViewDeliver {
    private final List<RoomSimpleViewResponseDto> rooms;

    public RoomsSimpleViewDeliver(List<RoomSimpleViewResponseDto> rooms) {
        this.rooms = rooms;
    }

    public List<RoomSimpleViewResponseDto> getRooms() {
        return rooms;
    }
}
