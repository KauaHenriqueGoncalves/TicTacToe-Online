package com.tic.tac.toe.presentation.http.controller;

import com.tic.tac.toe.application.service.UserServiceImpl;
import com.tic.tac.toe.domain.service.UserService;
import com.tic.tac.toe.infrastructure.persistence.jpa.repository.UserJpaRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthController {
    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);
    private static final UserService userService;
    private static final String PREFIX = "/auth";

    static {
        userService = new UserServiceImpl(UserJpaRepository.getFactory());
        log.info("Instance {} initialized.", AuthController.class.getSimpleName());
    }

    private AuthController() {
    }

    public static void register(Javalin server, String path) {
        String route = path + PREFIX;
    }

    private static void register(Context ctx) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    private static void login(Context ctx) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    private static void logout(Context ctx) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }
}
