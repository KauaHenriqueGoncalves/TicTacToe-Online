package com.tic.tac.toe;

import com.tic.tac.toe.application.service.*;
import com.tic.tac.toe.domain.repositoy.GameRepository;
import com.tic.tac.toe.domain.repositoy.RoomPlayerRepository;
import com.tic.tac.toe.domain.repositoy.RoomRepository;
import com.tic.tac.toe.domain.repositoy.UserRepository;
import com.tic.tac.toe.domain.service.*;
import com.tic.tac.toe.infrastructure.persistence.jpa.JpaUtil;
import com.tic.tac.toe.infrastructure.persistence.jpa.repository.GameJpaRepository;
import com.tic.tac.toe.infrastructure.persistence.jpa.repository.RoomJpaRepository;
import com.tic.tac.toe.infrastructure.persistence.jpa.repository.RoomPlayerJpaRepository;
import com.tic.tac.toe.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.tic.tac.toe.infrastructure.security.JwtService;
import com.tic.tac.toe.presentation.http.controller.AuthController;
import com.tic.tac.toe.presentation.http.middleware.AuthorizedHttpMiddleware;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.middleware.AuthorizedSocketMiddleware;
import com.tic.tac.toe.presentation.socket.room.RoomManager;

public final class AppContext {
    private static AppContext instance = null;
    public final JwtService jwtService;
    public final UserRepository userRepository;
    public final RoomRepository roomRepository;
    public final RoomPlayerRepository roomPlayerRepository;
    public final GameRepository gameRepository;
    public final AuthService authService;
    public final UserService userService;
    public final RoomService roomService;
    public final RoomPlayerService roomPlayerService;
    public final GameService gameService;
    public final AuthorizedHttpMiddleware authorizedHttpMiddleware;
    public final AuthController authController;
    public final AuthorizedSocketMiddleware authorizedSocketMiddleware;
    public final ConnectionManager connectionManager;
    public final RoomManager roomManager;

    private AppContext() {
        // infrastructure layer
        this.jwtService = JwtService.getFactory();
        this.userRepository = new UserJpaRepository(JpaUtil.getFactory());
        this.roomRepository = new RoomJpaRepository(JpaUtil.getFactory());
        this.roomPlayerRepository = new RoomPlayerJpaRepository(JpaUtil.getFactory());
        this.gameRepository = new GameJpaRepository(JpaUtil.getFactory());

        // application layer
        this.authService = new AuthServiceImpl(userRepository, jwtService);
        this.userService = new UserServiceImpl(userRepository);
        this.roomService = new RoomServiceImpl(roomRepository, roomPlayerRepository, userRepository);
        this.roomPlayerService = new RoomPlayerServiceImpl(roomPlayerRepository);
        this.gameService = new GameServiceImpl(gameRepository, roomRepository, roomPlayerRepository);

        // presentation layer
        // http
        this.authorizedHttpMiddleware = new AuthorizedHttpMiddleware(this.jwtService);
        this.authController = new AuthController(this.authService, "/auth");

        // websocket
        this.authorizedSocketMiddleware = new AuthorizedSocketMiddleware(this.jwtService);
        this.connectionManager = new ConnectionManager();
        this.roomManager = new RoomManager();
    }

    public static AppContext getInstance() {
        return instance == null ? instance = new AppContext() : instance;
    }
}
