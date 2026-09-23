package com.tic.tac.toe.presentation.socket.middleware;

import com.tic.tac.toe.infrastructure.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.tic.tac.toe.presentation.socket.util.SocketUtil.extractToken;

public final class AuthorizedSocketMiddleware {
    private static final Logger log = LoggerFactory.getLogger(AuthorizedSocketMiddleware.class);
    private final JwtService jwtService;

    public AuthorizedSocketMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String authenticate(WebSocket conn, ClientHandshake handshake) throws JwtException, Exception {
        String accessToken = extractToken(handshake.getResourceDescriptor());
        if (accessToken == null) {
            log.warn("Authentication required");
            conn.close(1008, "Authentication required");
            throw new Exception("Authentication required");
        }
        return jwtService.validate(accessToken);
    }
}
