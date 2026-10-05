package com.tic.tac.toe.domain.service;

import com.tic.tac.toe.domain.entity.Game;
import java.util.UUID;

public interface GameService {
    Game start(UUID roomId);
    Game findByRoomId(UUID roomId);
    Game play(UUID roomId, UUID userId, int position);
}
