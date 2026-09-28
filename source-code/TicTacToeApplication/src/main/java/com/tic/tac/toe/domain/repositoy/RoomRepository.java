package com.tic.tac.toe.domain.repositoy;

import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import java.util.List;
import java.util.UUID;

public interface RoomRepository {
    List<Room> findAll();
    List<Room> findAllByStatus(RoomStatus status);
    List<User> findAllUsersByRoomId(UUID roomId);
    Room findById(UUID id);
    Room save(Room room);
    void deleteById(UUID id);
}
