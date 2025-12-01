package com.oneonone.userservice.presentation.dto.response;

public record LoginResponse (
        String token
) {
    public static LoginResponse from(String token) {
        return new LoginResponse(token);
    }
}