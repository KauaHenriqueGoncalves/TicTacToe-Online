package com.tic.tac.toe.application.dto.response;

public final class AccessTokenResponseDto {
    private String accessToken;

    public AccessTokenResponseDto() {
    }

    public AccessTokenResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
