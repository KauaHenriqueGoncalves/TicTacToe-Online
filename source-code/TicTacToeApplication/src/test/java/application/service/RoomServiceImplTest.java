package application.service;

import com.tic.tac.toe.application.service.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomServiceImplTest {
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomPlayerRepository roomPlayerRepository;
    @Mock
    private UserRepository userRepository;
    private RoomServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RoomServiceImpl(roomRepository, roomPlayerRepository, userRepository);
    }

    @Test
    void findAllDelegates() {
        List<Room> rooms = Collections.singletonList(Room.init("A", UUID.randomUUID()));
        when(roomRepository.findAll()).thenReturn(rooms);
        assertSame(rooms, service.findAll());
    }

    @Test
    void findAllByStatusParsesIgnoringCaseAndSpaces() {
        service.findAllByStatus("  waiting ");
        verify(roomRepository).findAllByStatus(RoomStatus.WAITING);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "PLAYING"})
    void findAllByStatusRejectsInvalid(String status) {
        assertThrows(InputInvalidException.class, () -> service.findAllByStatus(status));
        verifyNoInteractions(roomRepository);
    }

    @Test
    void findByIdAndFindAllUsersByRoomId() {
        Room room = Room.init("A", UUID.randomUUID());
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        assertSame(room, service.findById(room.getId()));
        assertSame(room, service.findAllUsersByRoomId(room.getId()));
    }

    @Test
    void findByIdNotFound() {
        when(roomRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.findById(UUID.randomUUID()));
        assertThrows(NotFoundException.class, () -> service.findAllUsersByRoomId(UUID.randomUUID()));
    }

    @Test
    void createSavesRoomAndOwnerAsPlayer() {
        User owner = new User(UUID.randomUUID(), "dono", "d@d.com", "h", LocalDateTime.now());
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        Room room = service.create(new CreateRoomRequestDto("  Minha sala  ", owner.getId()));

        assertEquals("Minha sala", room.getName());
        assertEquals(owner.getId(), room.getOwnerId());
        ArgumentCaptor<RoomPlayer> captor = ArgumentCaptor.forClass(RoomPlayer.class);
        verify(roomPlayerRepository).save(captor.capture());
        assertSame(owner, captor.getValue().getUser());
        assertSame(room, captor.getValue().getRoom());
    }

    @Test
    void createValidations() {
        assertThrows(InputInvalidException.class, () -> service.create(null));
        assertThrows(InputInvalidException.class, () -> service.create(new CreateRoomRequestDto(null, UUID.randomUUID())));
        assertThrows(InputInvalidException.class, () -> service.create(new CreateRoomRequestDto("   ", UUID.randomUUID())));
        assertThrows(InputInvalidException.class, () -> service.create(new CreateRoomRequestDto("Sala", null)));
        verifyNoInteractions(roomRepository, roomPlayerRepository, userRepository);
    }

    @Test
    void createFailsWhenOwnerNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(new CreateRoomRequestDto("Sala", UUID.randomUUID())));
        verifyNoInteractions(roomRepository);
    }

    @Test
    void updateCopiesFieldsAndSaves() {
        UUID id = UUID.randomUUID();
        Room stored = new Room(id, "Antigo", RoomStatus.WAITING, LocalDateTime.now());
        Room changes = new Room(id, "Novo", UUID.randomUUID(), RoomStatus.FINISHED,
                UUID.randomUUID(), UUID.randomUUID(), "vencedor", LocalDateTime.now(), null, new HashSet<>());
        when(roomRepository.findById(id)).thenReturn(Optional.of(stored));
        when(roomRepository.save(stored)).thenReturn(stored);

        Room updated = service.update(changes);

        assertSame(stored, updated);
        assertEquals("Novo", stored.getName());
        assertEquals(changes.getOwnerId(), stored.getOwnerId());
        assertEquals(RoomStatus.FINISHED, stored.getStatus());
        assertEquals(changes.getCurrentPlayerId(), stored.getCurrentPlayerId());
        assertEquals(changes.getWinnerId(), stored.getWinnerId());
        assertEquals("vencedor", stored.getWinnerName());
        assertSame(changes.getPlayers(), stored.getPlayers());
    }

    @Test
    void updateValidations() {
        assertThrows(InputInvalidException.class, () -> service.update(null));
        assertThrows(InputInvalidException.class, () -> service.update(new Room()));
        when(roomRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.update(Room.init("x", UUID.randomUUID())));
    }

    @Test
    void deleteRemovesExistingRoom() {
        Room room = Room.init("A", UUID.randomUUID());
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        service.delete(room.getId());
        verify(roomRepository).deleteById(room.getId());
    }

    @Test
    void deleteNotFound() {
        when(roomRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.delete(UUID.randomUUID()));
        verify(roomRepository, never()).deleteById(any());
    }
}
