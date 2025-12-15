package com.oneonone.userservice.application.command;

import com.oneonone.common.enums.UserRole;

public record SignupCommand(
        String username,
        String password,
        String email,
        String nickname,
        String slackId,
        UserRole role
) {
}
