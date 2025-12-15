package com.oneonone.userservice.presentation.dto.request;

import com.oneonone.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @Pattern(regexp = "^[a-z0-9]{4,10}$",
                message = "아이디는 4~10자의 소문자 알파벳과 숫자만 사용할 수 있습니다.")
        String username,
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
                message = "비밀번호는 영문 대소문자와 숫자, 특수문자를 포함한 8~15자로 구성되어야 합니다.")
        String password,
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,
        @NotBlank(message = "슬랙 ID는 필수입니다.")
        String slackId,
        UserRole role
) {
}
