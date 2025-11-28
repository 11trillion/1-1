package com.oneonone.userservice.presentation.dto.request;

import com.oneonone.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {
    @Pattern(regexp = "^[a-z0-9]{4,10}$",
            message = "아이디는 4~10자의 소문자 알파벳과 숫자만 사용할 수 있습니다.")
    private String username;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
            message = "비밀번호는 영문 대소문자와 숫자, 특수문자를 포함한 8~15자로 구성되어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotBlank(message = "슬랙 ID는 필수입니다.")
    private String slackId;

    private UserRole role;
}
