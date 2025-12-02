package com.oneonone.userservice.presentation.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
                message = "비밀번호는 영문 대소문자와 숫자, 특수문자를 포함한 8~15자로 구성되어야 합니다.")
        String password,
        String nickname,
        String slackId
) {
}
