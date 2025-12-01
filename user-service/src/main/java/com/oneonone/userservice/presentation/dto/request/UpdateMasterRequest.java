package com.oneonone.userservice.presentation.dto.request;

import com.oneonone.common.enums.UserRole;
import com.oneonone.userservice.domain.enums.UserStatus;

public record UpdateMasterRequest(
        String nickname,
        UserRole role,
        UserStatus status,
        Long pointBalance,
        String slackId
) {
}
