package com.tic.tac.toe.infrastructure.security;

import com.tic.tac.toe.infrastructure.config.Environment;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Key;
import java.util.Date;

public final class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final JwtService FACTORY;
    private static final String JWT_SECRET;
    private final Key key;

    static {
        JWT_SECRET = Environment.get("JWT_SECRET");
        FACTORY = new JwtService(JWT_SECRET);
        log.info("Instance {} initialized.", JwtService.class.getSimpleName());
    }

    private JwtService(String secret) {
        this.key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    public static JwtService getFactory() {
        return FACTORY;
    }

    public String generate(String userId, long expirationMillis) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expirationMillis); // expired wont implemented (that's a simple project)
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * @throws JwtException if the token is invalid or expired.
     */
    public String validate(String accessToken) throws JwtException {
        Claims claim = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
        return claim.getSubject();
    }
}
