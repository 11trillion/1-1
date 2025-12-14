package com.oneonone.userservice.presentation.dto.response;

import com.oneonone.common.enums.UserRole;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.enums.UserStatus;
public record SignupResponse(
        String username,
        String email,
        String nickname,
        UserRole role,
        UserStatus status,
        String slackId
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getSlackId()
        );
    }
}
