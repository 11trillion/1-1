package com.oneonone.userservice.presentation.dto.response;

import com.oneonone.userservice.application.dto.UserInfo;

public record PointResponse(
        Long userId,
        Long pointBalance
) {
    public static PointResponse from(UserInfo userInfo) {
        return new PointResponse(
                userInfo.userId(),
                userInfo.pointBalance());
    }
}
