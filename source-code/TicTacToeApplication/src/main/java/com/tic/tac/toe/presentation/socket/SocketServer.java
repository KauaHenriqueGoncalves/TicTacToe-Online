package com.tic.tac.toe.presentation.socket;

import com.tic.tac.toe.infrastructure.config.Environment;
import com.tic.tac.toe.presentation.socket.connection.SocketManager;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;

public final class SocketServer extends WebSocketServer {
    private static final Logger log =
            LoggerFactory.getLogger(SocketServer.class);
    private static final SocketServer server;
    private static final SocketManager SOCKET_MANAGER;
    private static final RoomManager ROOM_MANAGER;
    private static final int PORT;

    static {
        try {
            PORT = Integer.parseInt(Environment.get("SOCKET_PORT"));
            server = new SocketServer(PORT);
            SOCKET_MANAGER = new SocketManager();
            ROOM_MANAGER = new RoomManager();
            server.start();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static SocketServer getServer() {
        return server;
    }

    private SocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        log.info("WebSocketServer started successfully. [port={}] [url={}]",
                PORT, "ws://localhost:" + PORT);
    }
}
