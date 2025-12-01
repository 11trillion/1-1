package com.oneonone.pointservice.domain;

import com.oneonone.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "해당 포인트를 찾을 수 없습니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "P002", "포인트 금액은 0보다 커야 합니다."),
    STATUS_CANNOT_CHANGE(HttpStatus.BAD_REQUEST, "P003", "SUCCESS 상태는 변경 불가"),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "P004", "유효하지 않은 status 값입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
