package com.oneonone.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 시스템 전체에서 사용하는 에러 코드 정의
 *
 * 에러 코드를 중앙 집중식으로 관리하며,
 * Http 상태 코드와 메시지를 합께 정의하여 일관성을 보장합니다.
 * 단일 책임 원칙(SRP)에 따라 에러 정의만 담당합니다.
 */
@Getter
public enum CommonErrorCode implements ErrorCode{

    // Comon 에러 (1000번대)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C003","요청 형식이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004","요청하신 경로를 찾을 수 없습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "C005","Validation Error");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CommonErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
