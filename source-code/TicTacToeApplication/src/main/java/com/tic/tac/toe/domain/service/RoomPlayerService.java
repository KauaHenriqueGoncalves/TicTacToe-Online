package com.tic.tac.toe.domain.service;

import java.util.UUID;

public interface RoomPlayerService {
    void deleteById(UUID playerId);
    void deleteByRoomIdAndUserId(UUID roomId, UUID userId);
}
