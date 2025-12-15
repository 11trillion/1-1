package com.oneonone.bettingservice.domain.vo;

import com.oneonone.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BettingErrorCode implements ErrorCode {
    BETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "B001","유효하지 않은 베팅 ID 입니다."),
    BETTING_UPDATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "B002", "베팅 수정 데이터 오류")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
