package com.tic.tac.toe.domain.service;

import com.tic.tac.toe.domain.entity.pk.RoomPlayer;

import java.util.Map;
import java.util.UUID;

public interface RoomPlayerService {
    Map<UUID, Long> countGroupedByRoomId();
    RoomPlayer create(RoomPlayer roomPlayer);
    RoomPlayer toggleReady(UUID roomId, UUID userId);
    void deleteById(UUID playerId);
    void deleteByRoomIdAndUserId(UUID roomId, UUID userId);
}
