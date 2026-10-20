package infrastructure.persistence.jpa.repository;

import com.tic.tac.toe.infrastructure.persistence.jpa.repository.*;
import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Simula falhas do banco (EntityManager lancando excecao) para cobrir os blocos catch
 * dos repositorios: rollback da transacao, fechamento do EntityManager e traducao
 * para RepositoryException.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JpaRepositoryErrorPathsTest {
    @Mock
    private EntityManagerFactory emf;
    @Mock
    private EntityManager em;
    @Mock
    private EntityTransaction tx;

    private final RuntimeException dbError = new RuntimeException("db fora do ar");

    @BeforeEach
    void setUp() {
        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(tx);
        when(tx.isActive()).thenReturn(true);
        when(em.createQuery(anyString(), any())).thenThrow(dbError);
        when(em.find(any(), any())).thenThrow(dbError);
        when(em.merge(any())).thenThrow(dbError);
        doThrow(dbError).when(em).persist(any());
    }

    private void assertClosedAndRolledBack() {
        verify(tx, atLeastOnce()).rollback();
        verify(em, atLeastOnce()).close();
    }

    // ---------- UserJpaRepository ----------
    @Test
    void userRepositoryErrors() {
        UserJpaRepository repo = new UserJpaRepository(emf);
        assertThrows(RepositoryException.class, repo::findAll);
        assertThrows(RepositoryException.class, () -> repo.findById(UUID.randomUUID()));
        assertFalse(repo.findByEmail("x@x.com").isPresent());
        assertThrows(RepositoryException.class, () -> repo.save(new User()));
        assertThrows(RepositoryException.class, () -> repo.save(new User(UUID.randomUUID(), "u", "e", "h", null)));
        assertClosedAndRolledBack();
    }

    @Test
    void userSaveWithoutIdUsesPersist() {
        doNothing().when(em).persist(any());
        UserJpaRepository repo = new UserJpaRepository(emf);
        User user = new User();
        assertSame(user, repo.save(user));
        verify(em).persist(user);
        verify(tx).commit();
    }

    // ---------- RoomJpaRepository ----------
    @Test
    void roomRepositoryErrors() {
        RoomJpaRepository repo = new RoomJpaRepository(emf);
        assertThrows(RepositoryException.class, repo::findAll);
        assertThrows(RepositoryException.class, () -> repo.findAllByStatus(RoomStatus.WAITING));
        assertThrows(RepositoryException.class, () -> repo.findAllUsersByRoomId(UUID.randomUUID()));
        assertThrows(RepositoryException.class, () -> repo.findById(UUID.randomUUID()));
        assertThrows(RepositoryException.class, () -> repo.save(Room.init("s", UUID.randomUUID())));
        assertThrows(RepositoryException.class, () -> repo.save(new Room()));
        assertThrows(RepositoryException.class, () -> repo.deleteById(UUID.randomUUID()));
        assertClosedAndRolledBack();
    }

    @Test
    void roomErrorsWithInactiveTransactionDoNotRollback() {
        when(tx.isActive()).thenReturn(false);
        RoomJpaRepository repo = new RoomJpaRepository(emf);
        assertThrows(RepositoryException.class, () -> repo.save(Room.init("s", UUID.randomUUID())));
        assertThrows(RepositoryException.class, () -> repo.deleteById(UUID.randomUUID()));
        verify(tx, never()).rollback();
    }

    @Test
    void roomSaveWithoutIdUsesPersist() {
        doNothing().when(em).persist(any());
        RoomJpaRepository repo = new RoomJpaRepository(emf);
        Room room = new Room();
        room.setId(null);
        // getId() continua null depois do persist mockado; o log usa result.getId() sem problema
        assertSame(room, repo.save(room));
        verify(em).persist(room);
    }

    // ---------- RoomPlayerJpaRepository ----------
    @Test
    void roomPlayerRepositoryErrors() {
        RoomPlayerJpaRepository repo = new RoomPlayerJpaRepository(emf);
        UUID id = UUID.randomUUID();
        assertThrows(RepositoryException.class, () -> repo.findById(id));
        assertTrue(repo.findByRoomId(id).isEmpty());
        assertThrows(RepositoryException.class, () -> repo.save(RoomPlayer.init(Room.init("s", id), new User())));
        assertThrows(RepositoryException.class, () -> repo.save(new RoomPlayer()));
        assertThrows(RepositoryException.class, repo::countGroupedByRoomId);
        assertThrows(RepositoryException.class, () -> repo.toggleReady(id, id));
        assertThrows(RepositoryException.class, () -> repo.deleteById(id));
        assertThrows(RepositoryException.class, () -> repo.deleteByRoomIdAndUserId(id, id));
        assertClosedAndRolledBack();
    }

    @Test
    void roomPlayerErrorsWithInactiveTransaction() {
        when(tx.isActive()).thenReturn(false);
        RoomPlayerJpaRepository repo = new RoomPlayerJpaRepository(emf);
        UUID id = UUID.randomUUID();
        assertThrows(RepositoryException.class, () -> repo.save(new RoomPlayer()));
        assertThrows(RepositoryException.class, () -> repo.deleteById(id));
        assertThrows(RepositoryException.class, () -> repo.deleteByRoomIdAndUserId(id, id));
        assertThrows(RepositoryException.class, () -> repo.toggleReady(id, id));
        verify(tx, never()).rollback();
    }

    @Test
    void roomPlayerSaveWithoutIdUsesPersist() {
        doNothing().when(em).persist(any());
        RoomPlayerJpaRepository repo = new RoomPlayerJpaRepository(emf);
        RoomPlayer rp = new RoomPlayer();
        assertSame(rp, repo.save(rp));
        verify(em).persist(rp);
    }

    // ---------- GameJpaRepository ----------
    @Test
    void gameRepositoryErrors() {
        GameJpaRepository repo = new GameJpaRepository(emf);
        UUID id = UUID.randomUUID();
        assertThrows(RepositoryException.class, () -> repo.findByRoomId(id));
        assertThrows(RepositoryException.class, () -> repo.save(Game.init(Room.init("s", id), id, id)));
        assertThrows(RepositoryException.class, () -> repo.deleteByRoomId(id));
        assertClosedAndRolledBack();
    }

    @Test
    void gameErrorsWithInactiveTransaction() {
        when(tx.isActive()).thenReturn(false);
        GameJpaRepository repo = new GameJpaRepository(emf);
        UUID id = UUID.randomUUID();
        assertThrows(RepositoryException.class, () -> repo.save(Game.init(Room.init("s", id), id, id)));
        assertThrows(RepositoryException.class, () -> repo.deleteByRoomId(id));
        verify(tx, never()).rollback();
    }
}
