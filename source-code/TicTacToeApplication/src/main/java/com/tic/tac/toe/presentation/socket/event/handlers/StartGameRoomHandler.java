package com.tic.tac.toe.presentation.socket.event.handlers;

import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.domain.event.StartGameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StartGameRoomHandler implements EventHandler<StartGameEvent> {
    private static final Logger log = LoggerFactory.getLogger(StartGameRoomHandler.class);

    // TODO: Implement

    @Override
    public void handle(StartGameEvent event) {
        // OBS: Dont need to be Owner to start the game
    }
}
