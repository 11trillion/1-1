package com.oneonone.userservice.presentation.dto.response;

import com.oneonone.userservice.application.dto.UserInfo;

public record BalanceResponse(
        Long userId,
        Long pointBalance
) {
    public static BalanceResponse from(UserInfo userInfo) {
        return new BalanceResponse(
                userInfo.userId(),
                userInfo.pointBalance());
    }
}
