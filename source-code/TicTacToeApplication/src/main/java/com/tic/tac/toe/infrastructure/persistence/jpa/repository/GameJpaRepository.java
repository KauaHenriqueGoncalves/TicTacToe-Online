package com.tic.tac.toe.infrastructure.persistence.jpa.repository;

import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.exception.RepositoryException;
import com.tic.tac.toe.domain.repositoy.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GameJpaRepository implements GameRepository {
    private static final Logger log = LoggerFactory.getLogger(GameJpaRepository.class);
    private final EntityManagerFactory emf;

    public GameJpaRepository(EntityManagerFactory emf) {
        this.emf = emf;
        log.info("Instance {} initialized. [InstanceId={}]",
                GameJpaRepository.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public Optional<Game> findByRoomId(UUID roomId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Game> list = em.createQuery(
                        "SELECT g FROM Game g WHERE g.room.id = :roomId", Game.class)
                    .setParameter("roomId", roomId)
                    .getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } catch (RuntimeException ex) {
            log.error("Error finding game by roomId. [error={}]", ex.getMessage());
            throw new RepositoryException("Error finding game by roomId");
        } finally {
            em.close();
        }
    }

    @Override
    public Game save(Game game) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Game saved = em.merge(game);
            em.getTransaction().commit();
            return saved;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error saving game. [error={}]", ex.getMessage());
            throw new RepositoryException("Error saving game");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteByRoomId(UUID roomId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<Game> list = em.createQuery(
                            "SELECT g FROM Game g WHERE g.room.id = :roomId", Game.class)
                    .setParameter("roomId", roomId)
                    .getResultList();
            list.forEach(em::remove);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error deleting game. [error={}]", ex.getMessage());
            throw new RepositoryException("Error deleting game");
        } finally {
            em.close();
        }
    }
}
