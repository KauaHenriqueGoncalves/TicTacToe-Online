package com.tic.tac.toe.presentation.http.controller;

import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.application.dto.request.LoginRequestDto;
import com.tic.tac.toe.application.dto.response.AccessTokenResponseDto;
import com.tic.tac.toe.application.service.AuthServiceImpl;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.service.AuthService;
import com.tic.tac.toe.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.tic.tac.toe.infrastructure.security.JwtService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public final class AuthController {
    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);
    private static final AuthService authService;
    private static final String PREFIX = "/auth";

    static {
        authService = new AuthServiceImpl(UserJpaRepository.getFactory(), JwtService.getFactory());
        log.info("Instance {} initialized.", AuthController.class.getSimpleName());
    }

    private AuthController() {
    }

    public static void register(Javalin server, String path) {
        String route = path + PREFIX;
        server.post(route + "/register", AuthController::register);
        server.post(route + "/login", AuthController::login);
        server.post(route + "/logout", AuthController::logout);
    }

    private static void register(Context ctx) {
        CreateUserRequestDto body =
                ctx.bodyAsClass(CreateUserRequestDto.class);
        User user = authService.register(body);
        UUID newUserId = user.getId();
        ctx.header("Location", "/api/v1/users/" + newUserId);
        ctx.status(201);
    }

    private static void login(Context ctx) {
        LoginRequestDto body =
                ctx.bodyAsClass(LoginRequestDto.class);
        AccessTokenResponseDto response =
                authService.login(body);
        Cookie cookie =
                new Cookie("accessToken", response.getAccessToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        ctx.cookie(cookie);
        ctx.status(200);
    }

    private static void logout(Context ctx) {
        ctx.removeCookie("accessToken", "/");
        ctx.status(204);
    }
}
