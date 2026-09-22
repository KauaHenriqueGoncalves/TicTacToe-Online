package com.tic.tac.toe.presentation.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.infrastructure.config.Environment;
import com.tic.tac.toe.infrastructure.security.JwtService;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.event.EventConfig;
import com.tic.tac.toe.presentation.socket.exception.ExceptionSocketHandler;
import com.tic.tac.toe.presentation.socket.message.SocketMessageReceive;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import io.jsonwebtoken.JwtException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.UUID;

public final class SocketServer extends WebSocketServer {
    private static final Logger log =
            LoggerFactory.getLogger(SocketServer.class);
    private static final SocketServer SERVER;
    private static final ConnectionManager CONNECTION_MANAGER;
    private static final RoomManager ROOM_MANAGER;
    private static final ExceptionSocketHandler EXCEPTION_HANDLER;
    private static final EventDispatcher eventDispatcher;
    private static final JwtService jwtService;
    private static final ObjectMapper objectMapper;
    private static final int PORT;

    static {
        try {
            PORT = Integer.parseInt(Environment.get("SOCKET_PORT"));
            SERVER = new SocketServer(PORT);
            CONNECTION_MANAGER = ConnectionManager.getFactory();
            ROOM_MANAGER = RoomManager.getFactory();
            EXCEPTION_HANDLER = ExceptionSocketHandler.getFactory();
            eventDispatcher = EventConfig.register();
            jwtService = JwtService.getFactory();
            objectMapper = new ObjectMapper();
            SERVER.start();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static SocketServer getServer() {
        return SERVER;
    }

    private SocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        try {
            String accessToken = handshake.getFieldValue("accessToken");
            if (accessToken == null) {
                log.warn("Authentication required");
                conn.close(1008, "Authentication required");
                return;
            }
            String userId = jwtService.validate(accessToken);
            Connection connection =
                    Connection.create(conn, UUID.fromString(userId));
            CONNECTION_MANAGER.add(connection);
        } catch (JwtException ex) {
            log.warn("Invalid access token.");
            EXCEPTION_HANDLER.handle(conn, "CONNECTION", ex);
            conn.close(1008, "Invalid access token");
        } catch (Exception ex) {
            log.warn("Error to connection on websocket. [message={}]",
                    ex.getMessage());
            EXCEPTION_HANDLER.handle(conn, "CONNECTION", ex);
            conn.close(1008, "Error connection");
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        try {
            Connection connection =
                    CONNECTION_MANAGER.getByConnection(conn);
            if (connection == null) {
                log.warn("Close connection. [code={}] [reason={}]",
                        code, reason);
                return;
            }
            CONNECTION_MANAGER.remove(connection);
            UUID roomId = connection.getRoomId();
            if (roomId != null) {
                ROOM_MANAGER.leave(roomId, connection);
            }
            log.warn("Close connection. [connectionId={}] [userId={}] [code={}] [reason={}]",
                    connection.getId(), connection.getUserId(), code, reason);
        } catch (Exception ex) {
            log.warn("Error to close connection on websocket. [message={}]",
                    ex.getMessage());
            EXCEPTION_HANDLER.handle(conn, "CLOSE", ex);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            SocketMessageReceive request =
                    objectMapper.readValue(
                            message,
                            SocketMessageReceive.class
                    );

            // próximo passo: transformar request em DomainEvent

        } catch (Exception e) {
            EXCEPTION_HANDLER.handle(conn, "message", e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            log.error("WebSocket error. [connectionId={}]",
                    System.identityHashCode(conn), ex);
        } else {
            log.error("WebSocket server error.", ex);
        }
        EXCEPTION_HANDLER.handle(conn, "ERROR_CONNECTION", ex);
    }

    @Override
    public void onStart() {
        log.info("WebSocketServer started successfully. [port={}] [url={}]",
                PORT, "ws://localhost:" + PORT);
    }
}
