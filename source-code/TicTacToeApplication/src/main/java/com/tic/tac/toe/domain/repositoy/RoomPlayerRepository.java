package com.tic.tac.toe.domain.repositoy;

import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import java.util.List;
import java.util.UUID;

public interface RoomPlayerRepository {
    RoomPlayer findById(UUID id);
    List<RoomPlayer> findByRoomId(UUID roomId);
    RoomPlayer save(RoomPlayer roomPlayer);
}
