package com.tic.tac.toe.application.service;

import com.tic.tac.toe.application.dto.request.CreateRoomRequestDto;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import com.tic.tac.toe.domain.repositoy.RoomRepository;
import com.tic.tac.toe.domain.repositoy.UserRepository;
import com.tic.tac.toe.domain.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.UUID;

public final class RoomServiceImpl implements RoomService {
    private static final Logger log = LoggerFactory.getLogger(RoomServiceImpl.class);
    private final RoomRepository roomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final UserRepository userRepository;

    public RoomServiceImpl(RoomRepository roomRepository,
                           RoomPlayerRepository roomPlayerRepository,
                           UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.userRepository = userRepository;
        log.info("Instance {} initialized. [InstanceId={}]",
                RoomServiceImpl.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> findAllByStatus(String status) {
        RoomStatus roomStatus;
        try {
            roomStatus = RoomStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Invalid room status. [status={}]", status);
            throw new InputInvalidException("Invalid room status.");
        }
        return roomRepository.findAllByStatus(roomStatus);
    }

    @Override
    public Room findAllUsersByRoomId(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    @Override
    public Room create(CreateRoomRequestDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().trim().isEmpty()) {
            log.warn("Room name is required.");
            throw new InputInvalidException("Room name is required.");
        }
        if (dto.getOwnerId() == null) {
            log.warn("OwnerId is required.");
            throw new InputInvalidException("OwnerId is required.");
        }
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Owner not found"));
        Room room = roomRepository.save(Room.init(dto.getName().trim(), owner.getId()));
        roomPlayerRepository.save(RoomPlayer.init(room, owner));
        log.info("Room created. [roomId={}] [ownerId={}]", room.getId(), owner.getId());
        return room;
    }

    @Override
    public void delete(UUID roomId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        roomRepository.deleteById(roomId);
        log.info("Room deleted. [roomId={}]", roomId);
    }
}
