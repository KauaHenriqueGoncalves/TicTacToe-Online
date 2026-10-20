package application.dto;

import com.tic.tac.toe.application.dto.message.*;
import com.tic.tac.toe.application.dto.request.CreateRoomRequestDto;
import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.application.dto.request.LoginRequestDto;
import com.tic.tac.toe.application.dto.response.*;
import com.tic.tac.toe.domain.entity.Game;
import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.entity.enums.Mark;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void simpleMessageDelivers() {
        assertEquals("ok", new CreateRoomSuccessfulDeliver("ok").getMessage());
        assertEquals("empate", new GameDrawDeliver("empate").getMessage());
        assertEquals("saiu", new LeaveRoomSuccessfulDeliver("saiu").getMessage());

        GameStartedDeliver started = new GameStartedDeliver("---------", "x", "o", "X");
        assertEquals("---------", started.getBoard());
        assertEquals("x", started.getXPlayerId());
        assertEquals("o", started.getOPlayerId());
        assertEquals("X", started.getTurn());

        GameWinnerDeliver winner = new GameWinnerDeliver("joao", "id");
        assertEquals("joao", winner.getFrom());
        assertEquals("id", winner.getWinnerId());

        GlobalMessageDeliver global = new GlobalMessageDeliver("joao", "oi");
        assertEquals("joao", global.getFrom());
        assertEquals("oi", global.getMessage());

        LocalDateTime now = LocalDateTime.now();
        MessageRoomDeliver room = new MessageRoomDeliver("joao", "ola", now);
        assertEquals("joao", room.getFrom());
        assertEquals("ola", room.getMessage());
        assertEquals(now, room.getSentAt());

        List<RoomSimpleViewResponseDto> list = new ArrayList<>();
        assertSame(list, new RoomsSimpleViewDeliver(list).getRooms());
    }

    @Test
    void infoRoomDeliverOfMapsRoomGameAndPlayers() {
        UUID ownerId = UUID.randomUUID();
        User owner = new User(ownerId, "dono", "d@d.com", "h", LocalDateTime.now());
        Room room = Room.init("Sala", ownerId);
        room.setCurrentPlayerId(ownerId);
        room.setWinnerId(ownerId);
        room.setWinnerName("dono");
        RoomPlayer rp = RoomPlayer.init(room, owner);
        room.getPlayers().add(rp);
        Game game = Game.init(room, ownerId, UUID.randomUUID());
        room.setGame(game);

        InfoRoomDeliver info = InfoRoomDeliver.of(room);
        assertEquals(room.getId(), info.getId());
        assertEquals("Sala", info.getName());
        assertEquals(ownerId, info.getOwnerId());
        assertEquals(RoomStatus.WAITING, info.getStatus());
        assertEquals(ownerId, info.getCurrentPlayer());
        assertEquals(ownerId, info.getWinnerId());
        assertEquals("dono", info.getWinnerName());
        assertEquals(game.getId(), info.getGame().getId());
        assertEquals(1, info.getPlayers().size());

        RoomPlayerInfoRoomResponseDto p = info.getPlayers().get(0);
        assertEquals(rp.getId(), p.getId());
        assertEquals(ownerId, p.getUserId());
        assertEquals("dono", p.getUsername());
        assertFalse(p.getPlaying());
        assertFalse(p.getReady());
    }

    @Test
    void infoRoomDeliverWithoutGame() {
        Room room = Room.init("Sala", UUID.randomUUID());
        InfoRoomDeliver info = InfoRoomDeliver.of(room);
        assertNull(info.getGame());
        assertTrue(info.getPlayers().isEmpty());
    }

    @Test
    void gameSimpleView() {
        assertNull(GameSimpleViewResponseDto.of(null));
        UUID x = UUID.randomUUID();
        UUID o = UUID.randomUUID();
        Game game = Game.init(Room.init("s", x), x, o);
        GameSimpleViewResponseDto dto = GameSimpleViewResponseDto.of(game);
        assertEquals(game.getId(), dto.getId());
        assertEquals("---------", dto.getBoard());
        assertEquals(x, dto.getXPlayerId());
        assertEquals(o, dto.getOPlayerId());
        assertEquals(Mark.X, dto.getTurn());
    }

    @Test
    void roomSimpleViewUsesCounts() {
        Room a = Room.init("A", UUID.randomUUID());
        Room b = Room.init("B", UUID.randomUUID());
        Map<UUID, Long> counts = new HashMap<>();
        counts.put(a.getId(), 2L);

        List<RoomSimpleViewResponseDto> list = RoomSimpleViewResponseDto.of(Arrays.asList(a, b), counts);
        assertEquals(2, list.size());
        RoomSimpleViewResponseDto first = list.get(0);
        assertEquals(a.getId(), first.getId());
        assertEquals("A", first.getName());
        assertEquals(RoomStatus.WAITING, first.getStatus());
        assertEquals(2, first.getCurrentPlayer());
        assertEquals(Room.MAX_PLAYERS, first.getMaxPlayers());
        assertEquals(a.getCreatedAt(), first.getCreatedAt());
        assertEquals(0, list.get(1).getCurrentPlayer());

        RoomSimpleViewResponseDto empty = new RoomSimpleViewResponseDto();
        assertNull(empty.getId());
    }

    @Test
    void userSimpleView() {
        User u1 = new User(UUID.randomUUID(), "a", "a@a.com", "h", LocalDateTime.now());
        User u2 = new User(UUID.randomUUID(), "b", "b@b.com", "h", LocalDateTime.now());
        UserSimpleViewResponseDto dto = UserSimpleViewResponseDto.of(u1);
        assertEquals(u1.getId().toString(), dto.getId());
        assertEquals("a", dto.getUsername());
        assertEquals("a@a.com", dto.getEmail());
        assertEquals(2, UserSimpleViewResponseDto.of(Arrays.asList(u1, u2)).size());
        assertNull(new UserSimpleViewResponseDto().getId());
    }

    @Test
    void requestAndResponseDtos() {
        UUID owner = UUID.randomUUID();
        CreateRoomRequestDto room = new CreateRoomRequestDto("Sala", owner);
        assertEquals("Sala", room.getName());
        assertEquals(owner, room.getOwnerId());
        assertTrue(room.toString().contains("Sala"));
        assertTrue(room.toString().contains(owner.toString()));
        assertNull(new CreateRoomRequestDto().getName());

        CreateUserRequestDto user = new CreateUserRequestDto("  joao ", " j@j.com ", " 12345678 ");
        assertEquals("joao", user.getUsername());
        assertEquals("j@j.com", user.getEmail());
        assertEquals("12345678", user.getPassword());
        assertNull(new CreateUserRequestDto().getUsername());

        LoginRequestDto login = new LoginRequestDto("j@j.com", "pw");
        assertEquals("j@j.com", login.getEmail());
        assertEquals("pw", login.getPassword());
        assertNull(new LoginRequestDto().getEmail());

        assertEquals("tok", new AccessTokenResponseDto("tok").getAccessToken());
        assertNull(new AccessTokenResponseDto().getAccessToken());
    }
}
