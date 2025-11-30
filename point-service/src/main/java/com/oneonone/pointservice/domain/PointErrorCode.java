package com.oneonone.pointservice.domain;

import com.oneonone.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "P001", "포인트 금액은 0보다 커야 합니다."),
    STATUS_CANNOT_CHANGE(HttpStatus.BAD_REQUEST, "P002", "SUCCESS 상태는 변경 불가");

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
