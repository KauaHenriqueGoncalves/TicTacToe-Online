package com.tic.tac.toe.presentation.socket.middleware;

import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.infrastructure.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthorizedSocketMiddleware {
    private static final Logger log = LoggerFactory.getLogger(AuthorizedSocketMiddleware.class);
    private final JwtService jwtService;

    public AuthorizedSocketMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
        log.info("Instance {} initialized. [InstanceId={}]",
                AuthorizedSocketMiddleware.class.getSimpleName(), System.identityHashCode(this));
    }

    public String authenticate(WebSocket conn, ClientHandshake handshake) throws JwtException, Exception {
        String accessToken = extractToken(handshake.getResourceDescriptor());
        if (accessToken == null) {
            log.warn("Authentication required");
            throw new UnauthorizedException("Authentication required");
        }
        return jwtService.validate(accessToken);
    }

    private String extractToken(String resource) {
        if (resource == null) return null;
        int idx = resource.indexOf("accessToken=");
        if (idx == -1) return null;
        String token = resource.substring(idx + "accessToken=".length());
        int amp = token.indexOf('&');
        return amp == -1 ? token : token.substring(0, amp);
    }
}
