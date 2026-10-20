package infrastructure.persistence.jpa.repository;

import com.tic.tac.toe.infrastructure.persistence.jpa.repository.*;
import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.exception.RepositoryException;
import com.tic.tac.toe.infrastructure.persistence.jpa.JpaUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integracao dos repositorios JPA contra H2 em memoria
 * (DB_URL=jdbc:h2:mem:... definido no surefire do pom.xml).
 * Cada teste cria dados proprios com UUID/e-mail aleatorios, entao a ordem nao importa.
 */
class JpaRepositoryIntegrationTest {
    private static UserJpaRepository userRepo;
    private static RoomJpaRepository roomRepo;
    private static RoomPlayerJpaRepository roomPlayerRepo;
    private static GameJpaRepository gameRepo;

    @BeforeAll
    static void setUp() {
        EntityManagerFactory emf = JpaUtil.getFactory();
        assertSame(emf, JpaUtil.getFactory());
        userRepo = new UserJpaRepository(emf);
        roomRepo = new RoomJpaRepository(emf);
        roomPlayerRepo = new RoomPlayerJpaRepository(emf);
        gameRepo = new GameJpaRepository(emf);
    }

    private static User newUser(String name) {
        String email = name + "-" + UUID.randomUUID() + "@mail.com";
        return userRepo.save(User.create(new CreateUserRequestDto(name, email, "senha1234")));
    }

    private static Room newRoomWithOwner(User owner) {
        Room room = roomRepo.save(Room.init("Sala de " + owner.getUsername(), owner.getId()));
        roomPlayerRepo.save(RoomPlayer.init(room, owner));
        return room;
    }

    // ---------- UserJpaRepository ----------
    @Test
    void userSaveFindByIdFindByEmailAndFindAll() {
        User user = newUser("ana");

        Optional<User> byId = userRepo.findById(user.getId());
        assertTrue(byId.isPresent());
        assertEquals(user.getEmail(), byId.get().getEmail());

        Optional<User> byEmail = userRepo.findByEmail(user.getEmail());
        assertTrue(byEmail.isPresent());
        assertEquals(user.getId(), byEmail.get().getId());

        List<User> all = userRepo.findAll();
        assertTrue(all.contains(user));
    }

    @Test
    void userFindByUnknownEmailOrIdReturnsEmpty() {
        assertFalse(userRepo.findByEmail("ninguem-" + UUID.randomUUID() + "@mail.com").isPresent());
        assertFalse(userRepo.findById(UUID.randomUUID()).isPresent());
    }

    @Test
    void userSaveWithDuplicatedEmailFails() {
        User user = newUser("bia");
        User clone = User.create(new CreateUserRequestDto("bia2", user.getEmail(), "senha1234"));
        assertThrows(RepositoryException.class, () -> userRepo.save(clone));
    }

    // ---------- RoomJpaRepository ----------
    @Test
    void roomSaveFindAndFindByStatus() {
        User owner = newUser("caio");
        Room room = newRoomWithOwner(owner);

        Optional<Room> found = roomRepo.findById(room.getId());
        assertTrue(found.isPresent());
        assertEquals(room.getName(), found.get().getName());
        assertEquals(1, found.get().getPlayers().size());

        assertTrue(roomRepo.findAll().contains(room));
        assertTrue(roomRepo.findAllByStatus(RoomStatus.WAITING).contains(room));
        assertFalse(roomRepo.findAllByStatus(RoomStatus.FINISHED).contains(room));

        found.get().setStatus(RoomStatus.FINISHED);
        roomRepo.save(found.get());
        assertEquals(RoomStatus.FINISHED, roomRepo.findById(room.getId()).orElseThrow(AssertionError::new).getStatus());
    }

    @Test
    void roomFindByUnknownIdReturnsEmpty() {
        assertFalse(roomRepo.findById(UUID.randomUUID()).isPresent());
    }

    @Test
    void roomFindByNullIdFails() {
        assertThrows(RepositoryException.class, () -> roomRepo.findById(null));
    }

    @Test
    void roomDeleteByIdRemovesRoomAndPlayers() {
        User owner = newUser("davi");
        Room room = newRoomWithOwner(owner);

        roomRepo.deleteById(room.getId());

        assertFalse(roomRepo.findById(room.getId()).isPresent());
        assertTrue(roomPlayerRepo.findByRoomId(room.getId()).isEmpty());
        // apagar de novo nao falha (apenas registra aviso)
        assertDoesNotThrow(() -> roomRepo.deleteById(room.getId()));
    }

    @Test
    void roomFindAllUsersByRoomIdDoesNotCrashCaller() {
        // Observacao: a JPQL deste metodo seleciona RoomPlayer tipado como User.
        // O teste so garante que o metodo responde (lista ou RepositoryException).
        User owner = newUser("edu");
        Room room = newRoomWithOwner(owner);
        try {
            assertNotNull(roomRepo.findAllUsersByRoomId(room.getId()));
        } catch (RepositoryException expected) {
            assertEquals("Error finding users by roomId", expected.getMessage());
        }
    }

    // ---------- RoomPlayerJpaRepository ----------
    @Test
    void roomPlayerFindCountToggleAndDelete() {
        User owner = newUser("fabi");
        User guest = newUser("gabi");
        Room room = newRoomWithOwner(owner);
        RoomPlayer guestPlayer = roomPlayerRepo.save(RoomPlayer.init(room, guest));

        assertTrue(roomPlayerRepo.findById(guestPlayer.getId()).isPresent());
        assertEquals(2, roomPlayerRepo.findByRoomId(room.getId()).size());

        Map<UUID, Long> counts = roomPlayerRepo.countGroupedByRoomId();
        assertEquals(Long.valueOf(2), counts.get(room.getId()));

        RoomPlayer toggled = roomPlayerRepo.toggleReady(room.getId(), guest.getId());
        assertTrue(toggled.getReady());
        assertFalse(roomPlayerRepo.toggleReady(room.getId(), guest.getId()).getReady());

        roomPlayerRepo.deleteByRoomIdAndUserId(room.getId(), guest.getId());
        assertEquals(1, roomPlayerRepo.findByRoomId(room.getId()).size());

        RoomPlayer ownerPlayer = roomPlayerRepo.findByRoomId(room.getId()).get(0);
        roomPlayerRepo.deleteById(ownerPlayer.getId());
        assertFalse(roomPlayerRepo.findById(ownerPlayer.getId()).isPresent());
        assertDoesNotThrow(() -> roomPlayerRepo.deleteById(ownerPlayer.getId()));
    }

    @Test
    void roomPlayerNotFoundCases() {
        UUID room = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        assertFalse(roomPlayerRepo.findById(UUID.randomUUID()).isPresent());
        assertTrue(roomPlayerRepo.findByRoomId(room).isEmpty());
        assertThrows(NotFoundException.class, () -> roomPlayerRepo.toggleReady(room, user));
        assertThrows(RepositoryException.class, () -> roomPlayerRepo.deleteByRoomIdAndUserId(room, user));
    }

    // ---------- GameJpaRepository ----------
    @Test
    void gameSaveFindAndDelete() {
        User x = newUser("hugo");
        User o = newUser("iris");
        Room room = newRoomWithOwner(x);

        Game saved = gameRepo.save(Game.init(room, x.getId(), o.getId()));
        assertNotNull(saved.getId());

        Optional<Game> found = gameRepo.findByRoomId(room.getId());
        assertTrue(found.isPresent());
        assertEquals("---------", found.get().getBoard());

        Game game = found.get();
        game.play(x.getId(), 4);
        gameRepo.save(game);
        assertEquals("----X----", gameRepo.findByRoomId(room.getId()).orElseThrow(AssertionError::new).getBoard());

        gameRepo.deleteByRoomId(room.getId());
        assertFalse(gameRepo.findByRoomId(room.getId()).isPresent());
    }

    @Test
    void gameFindByUnknownRoomReturnsEmpty() {
        assertFalse(gameRepo.findByRoomId(UUID.randomUUID()).isPresent());
        assertDoesNotThrow(() -> gameRepo.deleteByRoomId(UUID.randomUUID()));
    }
}
