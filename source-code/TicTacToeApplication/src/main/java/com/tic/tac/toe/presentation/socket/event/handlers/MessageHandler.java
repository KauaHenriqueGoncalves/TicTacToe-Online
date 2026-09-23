package com.tic.tac.toe.presentation.socket.event.handlers;

import com.tic.tac.toe.application.event.EventHandler;
import com.tic.tac.toe.domain.event.MessageEvent;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;

public final class MessageHandler implements EventHandler<MessageEvent> {
    private final ConnectionManager connectionManager;

    public MessageHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void handle(MessageEvent event) {
        connectionManager.broadcast("olá pessoal");
    }
}
