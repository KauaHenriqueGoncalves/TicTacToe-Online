package com.tic.tac.toe.application.service;

import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import com.tic.tac.toe.domain.service.RoomPlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
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
    public Map<UUID, Long> countGroupedByRoomId() {
        return roomPlayerRepository.countGroupedByRoomId();
    }

    @Override
    public RoomPlayer create(RoomPlayer roomPlayer) {
        if (
                roomPlayer == null ||
                roomPlayer.getId() == null ||
                roomPlayer.getRoom() == null ||
                roomPlayer.getUser() == null
        ) {
            log.warn("RoomPlayer is required");
            throw new RuntimeException("RoomPlayer is required");
        }
        RoomPlayer created = roomPlayerRepository.save(roomPlayer);
        log.info("RoomPlayer created. [roomPlayerId={}]", created.getId());
        return created;
    }

    @Override
    public RoomPlayer toggleReady(UUID roomId, UUID userId) {
        return roomPlayerRepository.toggleReady(roomId, userId);
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
        RoomPlayer rp = roomPlayerRepository.findByRoomId(roomId).stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("RoomPlayer not found"));
        roomPlayerRepository.deleteById(rp.getId());
    }
}
