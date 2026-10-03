package com.tic.tac.toe.infrastructure.persistence.jpa.repository;

import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.exception.RepositoryException;
import com.tic.tac.toe.domain.repositoy.RoomRepository;
import org.eclipse.persistence.config.CascadePolicy;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class RoomJpaRepository implements RoomRepository {
    private static final Logger log = LoggerFactory.getLogger(RoomJpaRepository.class);
    private final EntityManagerFactory emf;

    public RoomJpaRepository(EntityManagerFactory emf) {
        this.emf = emf;
        log.info("Instance {} initialized. [InstanceId={}]",
                RoomJpaRepository.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public List<Room> findAll() {
        log.debug("Finding all rooms");
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Room> query =
                    em.createQuery("SELECT r FROM Room r", Room.class);
            List<Room> result = query.getResultList();
            log.debug("Found {} rooms", result.size());
            return result;
        } catch (RuntimeException e) {
            log.error("Error finding all rooms. [error={}]", e.getMessage());
            throw new RepositoryException("Error finding all rooms");
        } finally {
            em.close();
        }
    }

    @Override
    public List<Room> findAllByStatus(RoomStatus status) {
        log.debug("Finding all rooms by status={}", status);
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery(
                    "SELECT DISTINCT r FROM Room r " +
                            "LEFT JOIN FETCH r.players " +
                            "WHERE r.status = :status", Room.class);
            query.setParameter("status", status);
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.REFRESH_CASCADE, CascadePolicy.CascadeAllParts);
            List<Room> result = query.getResultList();
            log.debug("Found {} rooms with status={}", result.size(), status);
            return result;
        } catch (RuntimeException e) {
            log.error("Error finding rooms by status={}. [error={}]", status, e.getMessage());
            throw new RepositoryException("Error finding all rooms by status");
        } finally {
            em.close();
        }
    }

    @Override
    public List<User> findAllUsersByRoomId(UUID roomId) {
        log.debug("Finding all users by roomId={}", roomId);
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM Room r JOIN r.players u WHERE r.id = :roomId",
                    User.class
            );
            query.setParameter("roomId", roomId);
            List<User> result = query.getResultList();
            log.debug("Found {} users for roomId={}", result.size(), roomId);
            return result;
        } catch (RuntimeException e) {
            log.error("Error finding users by roomId={}. [error={}]", roomId, e.getMessage());
            throw new RepositoryException("Error finding users by roomId");
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Room> findById(UUID id) {
        log.debug("Finding room by id={}", id);
        EntityManager em = emf.createEntityManager();
        try {
            Room room = em.find(Room.class, id);
            em.refresh(room);
            log.debug("Room {} found: {}", id, room != null);
            return Optional.ofNullable(room);
        } catch (RuntimeException e) {
            log.error("Error finding room by id={}; [error={}]", id, e.getMessage());
            throw new RepositoryException("Error finding room by id");
        } finally {
            em.close();
        }
    }

    @Override
    public Room save(Room room) {
        log.debug("Saving room: {}", room);
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Room result = null;
            if (room.getId() == null) {
                em.persist(room);
            } else {
                em.merge(room);
            }
            result = room;
            em.getTransaction().commit();
            log.info("Room saved successfully. [RoomId={}]", result.getId());
            return result;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                log.warn("Transaction rolled back for room: {}", room);
            }
            log.error("Error saving room: {}. [error={}]", room, e.getMessage());
            throw new RepositoryException("Error saving room");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting room by id={}", id);
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Room room = em.find(Room.class, id);
            if (room != null) {
                em.refresh(room);
                em.remove(room);
                log.info("Room deleted successfully. [RoomId={}]", id);
            } else {
                log.warn("Room not found for deletion. [RoomId={}]", id);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                log.warn("Transaction rolled back for delete. [RoomId={}]", id);
            }
            log.error("Error deleting room by id={}. [error={}]", id, e.getMessage());
            throw new RepositoryException("Error deleting room by id");
        } finally {
            em.close();
        }
    }
}
