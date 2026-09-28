package com.tic.tac.toe.infrastructure.persistence.jpa.repository;

import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.repositoy.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class RoomJpaRepository implements RoomRepository {
    private static final Logger log = LoggerFactory.getLogger(RoomJpaRepository.class);
    private final EntityManagerFactory emf;

    public RoomJpaRepository(EntityManagerFactory emf) {
        this.emf = emf;
        log.info("Instance {} initialized. [InstanceId={}]",
                RoomPlayerJpaRepository.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public List<Room> findAll() {
        return Collections.emptyList();
    }

    @Override
    public List<Room> findAllByStatus(RoomStatus status) {
        return Collections.emptyList();
    }

    @Override
    public List<User> findAllUsersByRoomId(UUID roomId) {
        return Collections.emptyList();
    }

    @Override
    public Room findById(UUID id) {
        return null;
    }

    @Override
    public Room save(Room room) {
        return null;
    }

    @Override
    public void deleteById(UUID id) {

    }
}
