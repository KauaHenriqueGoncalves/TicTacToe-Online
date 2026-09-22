package com.tic.tac.toe.application.service;

import com.tic.tac.toe.application.dto.response.AccessTokenResponseDto;
import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.application.dto.request.LoginRequestDto;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.repositoy.UserRepository;
import com.tic.tac.toe.domain.service.AuthService;
import com.tic.tac.toe.domain.validation.EmailValidation;
import com.tic.tac.toe.infrastructure.security.JwtService;
import com.tic.tac.toe.infrastructure.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public final class AuthServiceImpl implements AuthService {
    private static final Logger log =
            LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        log.info("Instance {} initialized. [InstanceId={}]",
                AuthServiceImpl.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public User register(CreateUserRequestDto dto) {
        log.info("Creating a new user. [username={}] [email={}]",
                dto.getUsername(), dto.getEmail());
        if (dto.getUsername() == null || dto.getPassword() == null) {
            log.warn("Username and password cant be null.");
            throw new InputInvalidException("Username and password cant be null.");
        }
        if (dto.getPassword().length() < 8) {
            log.warn("Password must be more than 8. [lengthSent={}]",
                    dto.getPassword().length());
            throw new InputInvalidException("Password must be more than 8.");
        }
        if (!EmailValidation.isValid(dto.getEmail())) {
            log.warn("Invalid email. [email={}]", dto.getEmail());
            throw new InputInvalidException("Invalid email.");
        }
        User newUser = userRepository.save(User.create(dto));
        log.info("User created successfully. [newUserId={}] [createdAt={}]",
                newUser.getId(), newUser.getCreatedAt());
        return newUser;
    }

    @Override
    public AccessTokenResponseDto login(LoginRequestDto dto) {
        log.info("Trying login. [email={}]", dto.getEmail());
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (!optionalUser.isPresent()) {
            log.warn("Credentials invalid. [email={}]", dto.getEmail());
            throw new UnauthorizedException("Credentials invalid.");
        }
        User user = optionalUser.get();
        boolean isPasswordMatched =
                PasswordHasher.matches(dto.getPassword(), user.getPasswordHash());
        if (!isPasswordMatched) {
            log.warn("Credentials invalid. [email={}]", dto.getEmail());
            throw new UnauthorizedException("Credentials invalid.");
        }
        String accessToken =
                jwtService.generate(user.getId().toString(), 72000);
        log.info("Login successfully. [userId={}]", user.getId());
        return new AccessTokenResponseDto(accessToken);
    }
}
