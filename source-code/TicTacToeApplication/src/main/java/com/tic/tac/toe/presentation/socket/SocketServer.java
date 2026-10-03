package com.tic.tac.toe.presentation.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.tac.toe.AppContext;
import com.tic.tac.toe.application.event.EventDispatcher;
import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.event.DomainEvent;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import com.tic.tac.toe.domain.event.RoomsByStatusEvent;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.infrastructure.config.Environment;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.event.EventRegister;
import com.tic.tac.toe.presentation.socket.event.SocketEventMapper;
import com.tic.tac.toe.presentation.socket.exception.ExceptionSocketHandler;
import com.tic.tac.toe.presentation.socket.message.SocketMessageReceive;
import com.tic.tac.toe.presentation.socket.middleware.AuthorizedSocketMiddleware;
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
    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private static ConnectionManager CONNECTION_MANAGER;
    private static RoomManager ROOM_MANAGER;
    private static AuthorizedSocketMiddleware AUTHORIZED_SOCKET_MIDDLEWARE;
    private static ExceptionSocketHandler EXCEPTION_HANDLER;
    private static ObjectMapper objectMapper;
    private static EventDispatcher eventDispatcher;
    private static AppContext appContext;
    private final int port;

    public static void start(AppContext context) {
        int port = Integer.parseInt(Environment.get("SOCKET_PORT"));
        SocketServer server = new SocketServer(port);
        CONNECTION_MANAGER = context.connectionManager;
        ROOM_MANAGER = context.roomManager;
        AUTHORIZED_SOCKET_MIDDLEWARE = context.authorizedSocketMiddleware;
        EXCEPTION_HANDLER = new ExceptionSocketHandler();
        objectMapper = new ObjectMapper();
        eventDispatcher = EventRegister.buildDispatcher(context);
        appContext = context;
        server.start();
    }

    private SocketServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        try {
            String userId =
                    AUTHORIZED_SOCKET_MIDDLEWARE.authenticate(conn, handshake);
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
        Connection connection = CONNECTION_MANAGER.getByConnection(conn);
        if (connection == null) {
            log.warn("Close connection. [code={}] [reason={}]", code, reason);
            return;
        }
        CONNECTION_MANAGER.remove(connection);
        UUID roomId = connection.getRoomId();
        if (roomId != null) {
            try {
                ROOM_MANAGER.leave(roomId, connection);
            } catch (Exception ex) {
                log.warn("Error leaving room in memory. [roomId={}] [error={}]", roomId, ex.getMessage());
            }
            try {
                appContext.roomPlayerService.deleteByRoomIdAndUserId(roomId, connection.getUserId());
            } catch (NotFoundException ex) {
                log.warn("RoomPlayer already removed. [roomId={}] [userId={}]", roomId, connection.getUserId());
            } catch (Exception ex) {
                log.error("Error deleting RoomPlayer. [roomId={}] [error={}]", roomId, ex.getMessage());
            }
            boolean roomDeleted = false;
            try {
                if (ROOM_MANAGER.get(roomId).getUsers().isEmpty()) {
                    appContext.roomManager.removeById(roomId);
                    appContext.roomService.delete(roomId);
                    roomDeleted = true;
                }
            } catch (Exception ex) {
                log.error("Error removing empty room. [roomId={}] [error={}]", roomId, ex.getMessage());
            }
            publishSafely(new RoomsByStatusEvent(RoomStatus.WAITING, connection.getUserId(), true));
            if (!roomDeleted) {
                publishSafely(new InfoRoomEvent(roomId, connection.getUserId()));
            }
        }
        log.warn("Close connection. [connectionId={}] [userId={}] [code={}] [reason={}]",
                connection.getId(), connection.getUserId(), code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            SocketMessageReceive request = objectMapper.readValue(message, SocketMessageReceive.class);
            Connection connection = CONNECTION_MANAGER.getByConnection(conn);
            DomainEvent event = SocketEventMapper.toDomainEvent(request, connection);
            eventDispatcher.dispatch(event);
        } catch (Exception e) {
            EXCEPTION_HANDLER.handle(conn, "message", e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error("WebSocket error.", ex);
        if (conn != null && conn.isOpen()) {
            EXCEPTION_HANDLER.handle(conn, "ERROR_CONNECTION", ex);
        }
    }

    @Override
    public void onStart() {
        log.info("WebSocketServer started successfully. [port={}] [url={}]",
                port, "ws://localhost:" + port);
    }

    private void publishSafely(DomainEvent event) {
        try {
            eventDispatcher.publish(event);
        } catch (Exception ex) {
            log.error("Error publishing event. [event={}] [error={}]", event.getEvent(), ex.getMessage());
        }
    }
}
