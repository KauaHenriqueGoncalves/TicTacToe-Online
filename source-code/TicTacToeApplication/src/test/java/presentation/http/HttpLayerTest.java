package presentation.http;

import com.tic.tac.toe.presentation.http.*;
import com.tic.tac.toe.AppContext;
import com.tic.tac.toe.domain.exception.*;
import com.tic.tac.toe.infrastructure.security.JwtService;
import com.tic.tac.toe.presentation.http.exception.ExceptionHttpHandler;
import com.tic.tac.toe.presentation.http.exception.StandardHttpException;
import com.tic.tac.toe.presentation.http.middleware.AuthorizedHttpMiddleware;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Camada HTTP: sobe o HttpServer real (porta HTTP_PORT do surefire) com o AppContext
 * apontando para o H2 em memoria e faz requisicoes de verdade.
 */
class HttpLayerTest {
    private static final String BASE = "http://localhost:18080";
    private static final String AUTH = BASE + "/api/v1/auth";
    private static Javalin errorApp;

    @BeforeAll
    static void startServers() {
        HttpServer.start(AppContext.getInstance());

        // Servidor auxiliar so para exercitar cada mapeamento do ExceptionHttpHandler
        errorApp = Javalin.create(config -> config.showJavalinBanner = false).start(0);
        ExceptionHttpHandler.register(errorApp);
        errorApp.get("/exists", ctx -> {
            throw new EntityAlreadyExistsException("ja existe");
        });
        errorApp.get("/invalid", ctx -> {
            throw new InputInvalidException("invalido");
        });
        errorApp.get("/notfound", ctx -> {
            throw new NotFoundException("nao achei");
        });
        errorApp.get("/repo", ctx -> {
            throw new RepositoryException("banco");
        });
        errorApp.get("/unauthorized", ctx -> {
            throw new UnauthorizedException("sem acesso");
        });
        errorApp.get("/runtime", ctx -> {
            throw new IllegalStateException("inesperado");
        });
    }

    @AfterAll
    static void stopErrorApp() {
        errorApp.stop();
    }

    // ---------- helpers ----------
    private static final class Response {
        final int status;
        final String body;
        final HttpURLConnection conn;

        Response(int status, String body, HttpURLConnection conn) {
            this.status = status;
            this.body = body;
            this.conn = conn;
        }
    }

    private static Response request(String method, String url, String json) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setInstanceFollowRedirects(false);
        if (json != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream out = conn.getOutputStream();
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.close();
        }
        int status = conn.getResponseCode();
        InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String body = "";
        if (in != null) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) buffer.write(chunk, 0, read);
            in.close();
            body = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
        return new Response(status, body, conn);
    }

    private static String userJson(String username, String email, String password) {
        return "{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private static String randomEmail() {
        return "http-" + UUID.randomUUID() + "@mail.com";
    }

    // ---------- AuthController + HttpServer ----------
    @Test
    void registerLoginAndLogout() throws Exception {
        String email = randomEmail();

        Response register = request("POST", AUTH + "/register", userJson("joao", email, "senha1234"));
        assertEquals(201, register.status);
        assertTrue(register.conn.getHeaderField("Location").startsWith("/api/v1/users/"));

        Response login = request("POST", AUTH + "/login", "{\"email\":\"" + email + "\",\"password\":\"senha1234\"}");
        assertEquals(200, login.status);
        String cookie = login.conn.getHeaderField("Set-Cookie");
        assertNotNull(cookie);
        assertTrue(cookie.startsWith("accessToken="));

        Response logout = request("POST", AUTH + "/logout", null);
        assertEquals(204, logout.status);
    }

    @Test
    void registerWithShortPasswordReturns400() throws Exception {
        Response response = request("POST", AUTH + "/register", userJson("joao", randomEmail(), "123"));
        assertEquals(400, response.status);
    }

    @Test
    void registerWithMissingFieldsReturns400() throws Exception {
        assertEquals(400, request("POST", AUTH + "/register", "{}").status);
    }

    @Test
    void registerWithDuplicatedEmailReturns500() throws Exception {
        String email = randomEmail();
        assertEquals(201, request("POST", AUTH + "/register", userJson("a", email, "senha1234")).status);
        assertEquals(500, request("POST", AUTH + "/register", userJson("b", email, "senha1234")).status);
    }

    @Test
    void registerWithMalformedJsonFails() throws Exception {
        assertTrue(request("POST", AUTH + "/register", "{isso nao e json").status >= 400);
    }

    @Test
    void loginWithWrongCredentialsReturns401() throws Exception {
        String email = randomEmail();
        request("POST", AUTH + "/register", userJson("joao", email, "senha1234"));
        assertEquals(401, request("POST", AUTH + "/login",
                "{\"email\":\"" + email + "\",\"password\":\"errada123\"}").status);
        assertEquals(401, request("POST", AUTH + "/login",
                "{\"email\":\"" + randomEmail() + "\",\"password\":\"senha1234\"}").status);
    }

    @Test
    void servesStaticFrontend() throws Exception {
        Response index = request("GET", BASE + "/index.html", null);
        assertEquals(200, index.status);
        assertTrue(index.body.toLowerCase().contains("<html"));
    }

    // ---------- ExceptionHttpHandler ----------
    private int errorStatus(String path) throws Exception {
        return request("GET", "http://localhost:" + errorApp.port() + path, null).status;
    }

    @Test
    void exceptionHandlerMapsEachException() throws Exception {
        assertEquals(409, errorStatus("/exists"));
        assertEquals(400, errorStatus("/invalid"));
        assertEquals(404, errorStatus("/notfound"));
        assertEquals(500, errorStatus("/repo"));
        assertEquals(401, errorStatus("/unauthorized"));
        assertEquals(500, errorStatus("/runtime"));
    }

    @Test
    void standardHttpExceptionGetters() {
        Instant now = Instant.now();
        StandardHttpException ex = new StandardHttpException(now, 404, "NOT_FOUND", "m", "/p");
        assertEquals(now, ex.getTimestamp());
        assertEquals(404, ex.getStatus());
        assertEquals("NOT_FOUND", ex.getError());
        assertEquals("m", ex.getMessage());
        assertEquals("/p", ex.getPath());
        assertNull(new StandardHttpException().getPath());
    }

    // ---------- AuthorizedHttpMiddleware ----------
    @Test
    void httpMiddlewareAcceptsValidToken() {
        JwtService jwt = mock(JwtService.class);
        when(jwt.validate("tok")).thenReturn("user-1");
        Context ctx = mock(Context.class);
        when(ctx.cookie("accessToken")).thenReturn("tok");

        new AuthorizedHttpMiddleware(jwt).authenticate(ctx);

        verify(ctx).attribute("userId", "user-1");
    }

    @Test
    void httpMiddlewareRejectsMissingOrInvalidToken() {
        JwtService jwt = mock(JwtService.class);
        when(jwt.validate("ruim")).thenThrow(new JwtException("invalido"));
        AuthorizedHttpMiddleware middleware = new AuthorizedHttpMiddleware(jwt);

        Context noCookie = mock(Context.class);
        assertThrows(UnauthorizedException.class, () -> middleware.authenticate(noCookie));

        Context badCookie = mock(Context.class);
        when(badCookie.cookie("accessToken")).thenReturn("ruim");
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> middleware.authenticate(badCookie));
        assertEquals("Invalid accessToken", ex.getMessage());
    }
}
