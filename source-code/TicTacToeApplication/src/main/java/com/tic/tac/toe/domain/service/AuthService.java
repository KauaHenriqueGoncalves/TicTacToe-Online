package com.tic.tac.toe.domain.service;

import com.tic.tac.toe.application.dto.response.AccessTokenResponseDto;
import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.application.dto.request.LoginRequestDto;
import com.tic.tac.toe.domain.entity.User;

public interface AuthService {
    User register(CreateUserRequestDto dto);
    AccessTokenResponseDto login(LoginRequestDto dto);
}
