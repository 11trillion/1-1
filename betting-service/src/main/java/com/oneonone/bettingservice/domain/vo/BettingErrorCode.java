package com.oneonone.bettingservice.domain.vo;

import com.oneonone.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BettingErrorCode implements ErrorCode {
    BETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "B001","유효하지 않은 베팅 ID 입니다."),
    BETTING_UPDATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "B002", "베팅 수정 데이터 오류"),
    BETTING_CLOSED(HttpStatus.NOT_FOUND, "B003","베팅이 종료된 경기입니다."),
    BETTING_REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"B004","Redis 조회 실패"),

    // 포인트 관련 에러
    POINT_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "B101", "포인트 조회에 실패했습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "B102", "포인트가 부족합니다."),
    POINT_DEDUCTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "B103", "포인트 차감에 실패했습니다."),
    POINT_REFUND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "B104", "포인트 환불에 실패했습니다."),

    // User Service 통신 관련 에러
    USER_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "B201", "User Service 통신 오류가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
