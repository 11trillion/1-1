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
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "P004", "유효하지 않은 status 값입니다."),

    // 보상 관련 에러
    ONLY_SUCCESS_CAN_BE_COMPENSATED(HttpStatus.BAD_REQUEST, "P005", "성공(SUCCESS) 상태의 포인트는 보상 처리할 수 없습니다."),
    ALREADY_COMPENSATING(HttpStatus.CONFLICT, "P006", "이미 보상 처리가 진행 중입니다."),
    NOT_IN_COMPENSATING_STATUS(HttpStatus.BAD_REQUEST, "P007", "보상 처리 중(COMPENSATING) 상태가 아닙니다."),
    CANNOT_UPDATE_DURING_COMPENSATION(HttpStatus.BAD_REQUEST, "P008", "보상 처리 중에는 수정할 수 없습니다."),
    ALREADY_COMPENSATED(HttpStatus.BAD_REQUEST, "P009", "이미 보상이 완료된 포인트입니다."),

    // 테스트/개발용
    FORCE(HttpStatus.BAD_REQUEST, "P999", "강제 오류")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
