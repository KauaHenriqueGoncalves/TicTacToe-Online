package com.tic.tac.toe.domain.service;

import com.tic.tac.toe.application.dto.request.CreateRoomRequestDto;
import com.tic.tac.toe.domain.entity.Room;
import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<Room> findAll();
    List<Room> findAllByStatus(String status);
    Room findById(UUID id);
    Room findAllUsersByRoomId(UUID roomId);
    Room create(CreateRoomRequestDto dto);
    Room update(Room room);
    void delete(UUID roomId);
}
