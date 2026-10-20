package infrastructure.security;

import com.tic.tac.toe.infrastructure.security.*;
import com.tic.tac.toe.infrastructure.config.Environment;
import com.tic.tac.toe.infrastructure.database.h2.H2Server;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * As variaveis de ambiente usadas aqui sao definidas no maven-surefire-plugin (pom.xml).
 */
class SecurityAndConfigTest {

    // ---------- Environment ----------
    @Test
    void environmentReadsVariables() {
        assertEquals("18080", Environment.get("HTTP_PORT"));
        assertEquals("eclipseLinkPersistence", Environment.get("ORM_PERSISTENCE"));
        assertNull(Environment.get("VARIAVEL_QUE_NAO_EXISTE_123"));
    }

    @Test
    void h2ServerPlaceholderCanBeInstantiated() {
        assertDoesNotThrow(H2Server::new);
    }

    // ---------- PasswordHasher ----------
    @Test
    void passwordHasherHashesAndMatches() {
        String hash = PasswordHasher.hash("senha1234");
        assertNotEquals("senha1234", hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordHasher.matches("senha1234", hash));
        assertFalse(PasswordHasher.matches("outra-senha", hash));
        assertNotEquals(hash, PasswordHasher.hash("senha1234"), "salt deve ser aleatorio");
    }

    // ---------- JwtService ----------
    @Test
    void jwtFactoryIsSingleton() {
        assertNotNull(JwtService.getFactory());
        JwtService first = JwtService.getFactory();
        assertSame(first, JwtService.getFactory());
    }

    @Test
    void jwtGenerateAndValidateRoundTrip() {
        String userId = UUID.randomUUID().toString();
        String token = JwtService.getFactory().generate(userId, 60000);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        assertEquals(userId, JwtService.getFactory().validate(token));
    }

    @Test
    void jwtValidateRejectsGarbage() {
        assertThrows(JwtException.class, () -> JwtService.getFactory().validate("nao.e.jwt"));
    }

    @Test
    void jwtValidateRejectsTokenSignedWithOtherKey() {
        String forged = Jwts.builder()
                .setSubject("intruso")
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256))
                .compact();
        assertThrows(JwtException.class, () -> JwtService.getFactory().validate(forged));
    }
}
