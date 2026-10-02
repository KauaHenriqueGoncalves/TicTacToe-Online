package com.tic.tac.toe.application.service;

import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import com.tic.tac.toe.domain.service.RoomPlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public final class RoomPlayerServiceImpl implements RoomPlayerService {
    private static final Logger log = LoggerFactory.getLogger(RoomPlayerServiceImpl.class);
    private final RoomPlayerRepository roomPlayerRepository;

    public RoomPlayerServiceImpl(RoomPlayerRepository roomPlayerRepository) {
        this.roomPlayerRepository = roomPlayerRepository;
        log.info("Instance {} initialized. [InstanceId={}]",
                RoomPlayerServiceImpl.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public void deleteById(UUID playerId) {
        roomPlayerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("RoomPlayer not found"));
        roomPlayerRepository.deleteById(playerId);
        log.info("RoomPlayer deleted. [roomPlayerId={}]", playerId);
    }

    @Override
    public void deleteByRoomIdAndUserId(UUID roomId, UUID userId) {
        RoomPlayer player = roomPlayerRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("RoomPlayer not found"));
        if (!player.getUser().getId().equals(userId)) {
            throw new RuntimeException("User is not the belong of the room");
        }
        roomPlayerRepository.deleteByRoomIdAndUserId(roomId, userId);
        log.info("RoomPlayer deleted by roomId and userId. [roomId={}] [userId={}]", roomId, userId);
    }
}
