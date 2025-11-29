package com.oneonone.userservice.presentation.dto.response;

import com.oneonone.common.enums.UserRole;
import com.oneonone.userservice.domain.entity.User;

public record UserResponse(
        String username,
        String nickname,
        UserRole role,
        String slackId
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getSlackId());
    }
}
