package com.oneonone.userservice.presentation.dto.response;

import com.oneonone.common.enums.UserRole;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private String username;
    private String nickname;
    private UserRole role;
    private UserStatus status;
    private String slackId;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .slackId(user.getSlackId())
                .build();
    }
}
