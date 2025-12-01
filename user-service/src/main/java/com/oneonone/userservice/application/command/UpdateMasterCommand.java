package com.oneonone.userservice.application.command;

import com.oneonone.common.enums.UserRole;
import com.oneonone.userservice.domain.enums.UserStatus;

public record UpdateMasterCommand(
        String nickname,
        UserRole role,
        UserStatus status,
        Long pointBalance,
        String slackId
) {
}
