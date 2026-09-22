package com.tic.tac.toe.application.dto.response;

import com.tic.tac.toe.domain.entity.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class UserSimpleViewResponseDto {
    private String id;
    private String username;
    private String email;

    public UserSimpleViewResponseDto() {
    }

    public UserSimpleViewResponseDto(UUID id, String username, String email) {
        this.id = id.toString();
        this.username = username;
        this.email = email;
    }

    public static UserSimpleViewResponseDto of(User u) {
        return new UserSimpleViewResponseDto(
                u.getId(),
                u.getUsername(),
                u.getEmail()
        );
    }

    public static List<UserSimpleViewResponseDto> of(List<User> u) {
        return u.stream()
                .map(UserSimpleViewResponseDto::of)
                .collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
