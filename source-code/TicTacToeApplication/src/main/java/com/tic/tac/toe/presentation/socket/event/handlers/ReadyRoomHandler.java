package com.tic.tac.toe.presentation.socket.event.handlers;

import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.application.event.EventPublisher;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import com.tic.tac.toe.domain.event.ReadyRoomEvent;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.service.RoomPlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public final class ReadyRoomHandler implements EventHandler<ReadyRoomEvent> {
    private static final Logger log = LoggerFactory.getLogger(ReadyRoomHandler.class);
    private final RoomPlayerService roomPlayerService;
    private final EventPublisher publisher;

    public ReadyRoomHandler(
            RoomPlayerService roomPlayerService,
            EventPublisher publisher
    ) {
        this.roomPlayerService = roomPlayerService;
        this.publisher = publisher;
        log.info("Instance {} initialized. [InstanceId={}]",
                ReadyRoomHandler.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public void handle(ReadyRoomEvent event) {
        UUID roomId = parse(event.getRoomId());
        UUID userId = parse(event.getUserId());
        roomPlayerService.toggleReady(roomId, userId);
        try {
            publisher.publish(new InfoRoomEvent(roomId, userId));
        } catch (RuntimeException e) {
            log.error("Error publishing InfoRoomEvent. [error={}]", e.getMessage());
        }
    }

    private UUID parse(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InputInvalidException("Invalid id");
        }
    }
}
