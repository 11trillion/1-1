package com.oneonone.userservice.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String token;

    public static LoginResponse from(String token) {
        return LoginResponse.builder()
                .token(token)
                .build();
    }
}