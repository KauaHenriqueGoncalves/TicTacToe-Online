package com.tic.tac.toe.domain.repositoy;

import com.tic.tac.toe.domain.entity.Game;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository {
    Optional<Game> findByRoomId(UUID roomId);
    Game save(Game game);
    void deleteByRoomId(UUID roomId);
}
