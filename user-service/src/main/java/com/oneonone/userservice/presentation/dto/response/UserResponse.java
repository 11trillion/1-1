package com.oneonone.userservice.presentation.dto.response;

import com.oneonone.common.enums.UserRole;
import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.enums.UserStatus;

public record UserResponse(
        String username,
        String nickname,
        UserRole role,
        UserStatus status,
        Long pointBalance,
        String slackId
) {
    public static UserResponse from(UserInfo userInfo) {
        return new UserResponse(
                userInfo.username(),
                userInfo.nickname(),
                userInfo.role(),
                userInfo.status(),
                userInfo.pointBalance(),
                userInfo.slackId());
    }
}
