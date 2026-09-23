package com.tic.tac.toe;

import com.tic.tac.toe.application.service.AuthServiceImpl;
import com.tic.tac.toe.domain.service.AuthService;
import com.tic.tac.toe.infrastructure.persistence.jpa.JpaUtil;
import com.tic.tac.toe.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.tic.tac.toe.infrastructure.security.JwtService;
import com.tic.tac.toe.presentation.http.controller.AuthController;
import com.tic.tac.toe.presentation.http.middleware.AuthorizedHttpMiddleware;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import java.util.concurrent.ConcurrentHashMap;

public final class AppContext {
    private static AppContext instance = null;
    public final JwtService jwtService;
    public final AuthService authService;
    public final AuthorizedHttpMiddleware authorizedHttpMiddleware;
    public final AuthController authController;
    public final ConnectionManager connectionManager;
    public final RoomManager roomManager;

    private AppContext() {
        this.jwtService = JwtService.getFactory();
        this.authService = new AuthServiceImpl(
                new UserJpaRepository(JpaUtil.getFactory()),
                jwtService
        );
        this.authorizedHttpMiddleware = new AuthorizedHttpMiddleware(this.jwtService);
        this.authController = new AuthController(this.authService, "/auth");
        this.connectionManager = new ConnectionManager(ConcurrentHashMap.newKeySet());
        this.roomManager = new RoomManager(ConcurrentHashMap.newKeySet());
    }

    public static AppContext getInstance() {
        return instance == null ? instance = new AppContext() : instance;
    }
}
