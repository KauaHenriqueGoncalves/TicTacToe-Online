package com.tic.tac.toe.presentation.http.controller;

import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.application.dto.response.UserSimpleViewResponseDto;
import com.tic.tac.toe.application.service.UserServiceImpl;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.service.UserService;
import com.tic.tac.toe.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.tic.tac.toe.presentation.http.middleware.AuthorizedHttpMiddleware;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public final class UserController {
    private static final Logger log =
            LoggerFactory.getLogger(UserController.class);
    private static final UserService userService;
    private static final AuthorizedHttpMiddleware authorizedHttpMiddleware;
    private static final String PREFIX = "/users";

    static {
        userService = new UserServiceImpl(UserJpaRepository.getFactory());
        authorizedHttpMiddleware = AuthorizedHttpMiddleware.getFactory();
        log.info("Instance {} initialized.", UserController.class.getSimpleName());
    }

    private UserController() {
    }

    public static void register(Javalin server, String path) {
        String route = path + PREFIX;
        server.before(route + "/*", authorizedHttpMiddleware::authenticate);
        server.get(route, UserController::findAll);
        server.post(route, UserController::create);
    }

    private static void findAll(Context ctx) {
        List<User> list = userService.findAll();
        ctx.status(200);
        ctx.json(UserSimpleViewResponseDto.of(list));
    }

    private static void findById(Context ctx) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    private static void create(Context ctx) {
        CreateUserRequestDto request =
                ctx.bodyAsClass(CreateUserRequestDto.class);
        userService.create(request);
        ctx.status(200);
    }
}
