package com.oneonone.userservice.presentation.dto.response;

import com.oneonone.common.enums.UserRole;
import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.enums.UserStatus;

public record MasterUserResponse(
        Long userId,
        String username,
        String nickname,
        UserRole role,
        UserStatus status,
        Long pointBalance,
        String slackId
) {
    public static MasterUserResponse from(UserInfo userInfo) {
        return new MasterUserResponse(
                userInfo.userId(),
                userInfo.username(),
                userInfo.nickname(),
                userInfo.role(),
                userInfo.status(),
                userInfo.pointBalance(),
                userInfo.slackId()
        );
    }
}
