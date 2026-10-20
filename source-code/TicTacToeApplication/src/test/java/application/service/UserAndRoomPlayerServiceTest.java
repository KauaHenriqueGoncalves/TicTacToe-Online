package application.service;

import com.tic.tac.toe.application.service.*;
import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import com.tic.tac.toe.domain.repositoy.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserAndRoomPlayerServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomPlayerRepository roomPlayerRepository;

    // ---------- UserServiceImpl ----------
    @Test
    void userFindAllAndFindById() {
        UserServiceImpl service = new UserServiceImpl(userRepository);
        User user = new User(UUID.randomUUID(), "u", "u@u.com", "h", LocalDateTime.now());
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertEquals(1, service.findAll().size());
        assertSame(user, service.findById(user.getId()));
    }

    @Test
    void userFindByIdNotFound() {
        UserServiceImpl service = new UserServiceImpl(userRepository);
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.findById(UUID.randomUUID()));
    }

    @Test
    void userCreate() {
        UserServiceImpl service = new UserServiceImpl(userRepository);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        User created = service.create(new CreateUserRequestDto("u", "u@u.com", "senha1234"));
        assertEquals("u", created.getUsername());
    }

    // ---------- RoomPlayerServiceImpl ----------
    private RoomPlayer roomPlayer() {
        Room room = Room.init("Sala", UUID.randomUUID());
        User user = new User(UUID.randomUUID(), "u", "u@u.com", "h", LocalDateTime.now());
        return RoomPlayer.init(room, user);
    }

    @Test
    void countAndToggleDelegate() {
        RoomPlayerServiceImpl service = new RoomPlayerServiceImpl(roomPlayerRepository);
        Map<UUID, Long> counts = new HashMap<>();
        when(roomPlayerRepository.countGroupedByRoomId()).thenReturn(counts);
        assertSame(counts, service.countGroupedByRoomId());

        RoomPlayer rp = roomPlayer();
        UUID roomId = rp.getRoom().getId();
        UUID userId = rp.getUser().getId();
        when(roomPlayerRepository.toggleReady(roomId, userId)).thenReturn(rp);
        assertSame(rp, service.toggleReady(roomId, userId));
    }

    @Test
    void createSavesValidRoomPlayer() {
        RoomPlayerServiceImpl service = new RoomPlayerServiceImpl(roomPlayerRepository);
        RoomPlayer rp = roomPlayer();
        when(roomPlayerRepository.save(rp)).thenReturn(rp);
        assertSame(rp, service.create(rp));
    }

    @Test
    void createRejectsIncompleteRoomPlayer() {
        RoomPlayerServiceImpl service = new RoomPlayerServiceImpl(roomPlayerRepository);
        assertThrows(RuntimeException.class, () -> service.create(null));
        assertThrows(RuntimeException.class, () -> service.create(new RoomPlayer()));

        RoomPlayer noRoom = roomPlayer();
        noRoom.setRoom(null);
        assertThrows(RuntimeException.class, () -> service.create(noRoom));

        RoomPlayer noUser = roomPlayer();
        noUser.setUser(null);
        assertThrows(RuntimeException.class, () -> service.create(noUser));
        verify(roomPlayerRepository, never()).save(any());
    }

    @Test
    void deleteById() {
        RoomPlayerServiceImpl service = new RoomPlayerServiceImpl(roomPlayerRepository);
        RoomPlayer rp = roomPlayer();
        when(roomPlayerRepository.findById(rp.getId())).thenReturn(Optional.of(rp));
        service.deleteById(rp.getId());
        verify(roomPlayerRepository).deleteById(rp.getId());

        when(roomPlayerRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.deleteById(UUID.randomUUID()));
    }

    @Test
    void deleteByRoomIdAndUserId() {
        RoomPlayerServiceImpl service = new RoomPlayerServiceImpl(roomPlayerRepository);
        RoomPlayer rp = roomPlayer();
        RoomPlayer other = roomPlayer();
        UUID roomId = rp.getRoom().getId();
        when(roomPlayerRepository.findByRoomId(roomId)).thenReturn(Arrays.asList(other, rp));

        service.deleteByRoomIdAndUserId(roomId, rp.getUser().getId());
        verify(roomPlayerRepository).deleteById(rp.getId());

        assertThrows(NotFoundException.class,
                () -> service.deleteByRoomIdAndUserId(roomId, UUID.randomUUID()));
    }
}
