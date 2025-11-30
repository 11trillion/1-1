package com.oneonone.userservice.exception;

import com.oneonone.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.FORBIDDEN, "U003", "비밀번호가 일치하지 않습니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "U004", "사용할 수 없는 닉네임입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
