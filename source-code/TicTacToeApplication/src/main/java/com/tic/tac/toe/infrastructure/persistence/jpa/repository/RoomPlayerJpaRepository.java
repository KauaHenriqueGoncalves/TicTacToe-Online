package com.tic.tac.toe.infrastructure.persistence.jpa.repository;

import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class RoomPlayerJpaRepository implements RoomPlayerRepository {
    private static final Logger log = LoggerFactory.getLogger(RoomPlayerJpaRepository.class);
    private final EntityManagerFactory emf;

    public RoomPlayerJpaRepository(EntityManagerFactory emf) {
        this.emf = emf;
        log.info("Instance {} initialized. [InstanceId={}]",
                RoomPlayerJpaRepository.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public RoomPlayer findById(UUID id) {
        return null;
    }

    @Override
    public List<RoomPlayer> findByRoomId(UUID roomId) {
        return Collections.emptyList();
    }

    @Override
    public RoomPlayer save(RoomPlayer roomPlayer) {
        return null;
    }
}
