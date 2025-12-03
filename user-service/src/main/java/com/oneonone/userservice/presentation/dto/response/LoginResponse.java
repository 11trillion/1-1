package com.oneonone.userservice.presentation.dto.response;

public record LoginResponse (
        String accessToken,
        String refreshToken
) {
    public static LoginResponse from(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, refreshToken);
    }
}