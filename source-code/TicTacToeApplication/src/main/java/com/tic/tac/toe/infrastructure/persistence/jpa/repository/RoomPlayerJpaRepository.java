package com.tic.tac.toe.infrastructure.persistence.jpa.repository;

import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.RepositoryException;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    public Optional<RoomPlayer> findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            RoomPlayer roomPlayer = em.find(RoomPlayer.class, id);
            return Optional.ofNullable(roomPlayer);
        } catch (RuntimeException ex) {
            log.error("Error trying to find RoomPlayer by Id. [error={}]", ex.getMessage());
            throw new RepositoryException("Error trying to find RoomPlayer by Id");
        } finally {
            em.close();
        }
    }

    @Override
    public List<RoomPlayer> findByRoomId(UUID roomId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<RoomPlayer> query = em.createQuery(
                    "SELECT rp FROM " + RoomPlayer.class.getSimpleName() + " rp " +
                    "WHERE rp.room.id = :roomId",
                    RoomPlayer.class
            );
            query.setParameter("roomId", roomId);
            return query.getResultList();
        } catch (RuntimeException ex) {
            log.error("Error trying to find RoomPlayer by RoomId. [error={}]", ex.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    @Override
    public RoomPlayer save(RoomPlayer roomPlayer) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            RoomPlayer result = null;
            if (roomPlayer.getId() == null) {
                em.persist(roomPlayer);
            } else {
                em.merge(roomPlayer);
            }
            result = roomPlayer;
            em.getTransaction().commit();
            return result;
        } catch (RuntimeException ex) {
            log.error("Error trying to save the RoomPlayer. [error={}]", ex.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                log.warn("Revert attempt to save the RoomPlayer");
            }
            throw new RepositoryException("Error trying to save the RoomPlayer");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting roomPlayer by id={}", id);
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            RoomPlayer player = em.find(RoomPlayer.class, id);
            if (player != null) {
                em.remove(player);
                log.info("RoomPlayer deleted successfully. [RoomPlayerId={}]", id);
            } else {
                log.warn("RoomPlayer not found for deletion. [RoomPlayerId={}]", id);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                log.warn("Transaction rolled back for delete. [RoomPlayerId={}]", id);
            }
            log.error("Error deleting roomPlayer by id={}. [error={}]", id, e.getMessage());
            throw new RepositoryException("Error deleting roomPlayer by id");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteByRoomIdAndUserId(UUID roomId, UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<RoomPlayer> query = em.createQuery(
                    "SELECT rp FROM " + RoomPlayer.class.getSimpleName() + " rp " +
                    "WHERE rp.room.id = :roomId AND rp.user.id = :userId",
                    RoomPlayer.class
            );
            query.setParameter("roomId", roomId);
            query.setParameter("userId", userId);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            log.error("Error trying to delete the RoomPlayer by roomId and userId. [error={}]", ex.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                log.warn("Revert attempt to dekete the RoomPlayer");
            }
            throw new RepositoryException("Error trying to delete the RoomPlayer");
        } finally {
            em.close();
        }
    }
}
