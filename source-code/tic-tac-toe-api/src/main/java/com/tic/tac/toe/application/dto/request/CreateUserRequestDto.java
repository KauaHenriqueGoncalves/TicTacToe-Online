package com.tic.tac.toe.application.dto.request;

public final class CreateUserRequestDto {
    private String username;
    private String email;
    private String password;

    public CreateUserRequestDto() {
    }

    public CreateUserRequestDto(String username, String email, String password) {
        this.username = username.trim();
        this.email = email.trim();
        this.password = password.trim();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
