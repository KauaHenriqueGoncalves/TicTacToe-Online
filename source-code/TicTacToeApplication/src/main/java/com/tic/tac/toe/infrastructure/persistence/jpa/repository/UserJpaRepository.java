package com.tic.tac.toe.infrastructure.persistence.jpa.repository;

import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.exception.RepositoryException;
import com.tic.tac.toe.domain.repositoy.UserRepository;
import com.tic.tac.toe.infrastructure.persistence.jpa.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class UserJpaRepository implements UserRepository {
    private static final Logger log =
            LoggerFactory.getLogger(UserJpaRepository.class);
    private static final UserJpaRepository FACTORY;
    private final EntityManagerFactory emf;

    static {
        FACTORY = new UserJpaRepository(JpaUtil.getFactory());
        log.info("Instance {} initialized.", UserJpaRepository.class.getSimpleName());
    }

    private UserJpaRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static UserJpaRepository getFactory() {
        return FACTORY;
    }

    @Override
    public List<User> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT u FROM " + User.class.getSimpleName() + " u";
            return em.createQuery(jpql, User.class).getResultList();
        } catch (RuntimeException ex) {
            log.error("Error trying to find all users. [error={}]", ex.getMessage());
            throw new RepositoryException("Error trying to find all users");
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            User user = em.find(User.class, id);
            return Optional.ofNullable(user);
        } catch (RuntimeException ex) {
            log.error("Error trying to find user by id. [error={}]", ex.getMessage());
            throw new RepositoryException("Error trying to find user by id");
        } finally {
            em.close();
        }
    }

    @Override
    public User save(User user) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            User result = null;
            if (user.getId() == null) {
                em.persist(user);
                result = user;
            } else {
                em.merge(user);
                result = user;
            }
            em.getTransaction().commit();
            return result;

        } catch (RuntimeException ex) {
            log.error("Error trying to save the user. [error={}]", ex.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                log.warn("Revert attempt to save the user");
            }
            throw new RepositoryException("Error trying to save the user");
        } finally {
            em.close();
        }
    }
}
