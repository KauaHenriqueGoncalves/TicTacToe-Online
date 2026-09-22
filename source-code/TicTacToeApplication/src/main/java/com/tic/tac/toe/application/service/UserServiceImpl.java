package com.tic.tac.toe.application.service;

import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.exception.NotFoundException;
import com.tic.tac.toe.domain.repositoy.UserRepository;
import com.tic.tac.toe.domain.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class UserServiceImpl implements UserService {
    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("Instance {} initialized. [InstanceId={}]",
                UserServiceImpl.class.getSimpleName(), System.identityHashCode(this));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            log.warn("user not found. [id={}]", id);
            throw new NotFoundException("Not found user");
        }
        return user.get();
    }

    @Override
    public User create(CreateUserRequestDto dto) {
        User user = User.create(dto);
        user = userRepository.save(user);
        return user;
    }
}
