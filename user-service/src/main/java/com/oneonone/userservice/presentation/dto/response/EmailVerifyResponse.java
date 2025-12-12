package com.oneonone.userservice.presentation.dto.response;

public record EmailVerifyResponse(
        String code
) {
    public static EmailVerifyResponse from(String code) {
        return new EmailVerifyResponse(code);
    }
}