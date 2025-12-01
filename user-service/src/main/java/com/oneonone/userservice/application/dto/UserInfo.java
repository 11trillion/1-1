package com.oneonone.userservice.application.dto;

import com.oneonone.common.enums.UserRole;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.enums.UserStatus;

public record UserInfo(
        Long userId,
        String username,
        String nickname,
        UserRole role,
        UserStatus status,
        Long pointBalance,
        String slackId
) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getPointBalance(),
                user.getSlackId()
        );
    }
}
