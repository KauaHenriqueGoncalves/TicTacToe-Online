package com.tic.tac.toe.presentation.http.middleware;

import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.infrastructure.security.JwtService;
import io.jsonwebtoken.JwtException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthorizedHttpMiddleware {
    private static final Logger log = LoggerFactory.getLogger(AuthorizedHttpMiddleware.class);
    private final JwtService jwtService;

    public AuthorizedHttpMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void authenticate(Context ctx) {
        String accessToken = ctx.cookie("accessToken");
        if (accessToken == null) {
            log.warn("Authentication required.");
            throw new UnauthorizedException("Authentication required");
        }
        try {
            String userId = jwtService.validate(accessToken);
            log.info("User authenticated with accessToken. [userId={}]", userId);
            ctx.attribute("userId", userId);
        } catch (JwtException e) {
            log.warn("Invalid accessToken.");
            throw new UnauthorizedException("Invalid accessToken");
        }
    }
}
