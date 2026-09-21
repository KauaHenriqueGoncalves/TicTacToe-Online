package com.tic.tac.toe.domain.service;

import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.domain.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> findAll();
    User findById(UUID id);
    User create(CreateUserRequestDto dto);
}
