package com.oneonone.gameservice.domain;

import com.oneonone.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GameErrorCode implements ErrorCode {
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND,"G001","해당 게임 ID를 찾을 수 없습니다"),
    START_TIME_ERROR(HttpStatus.BAD_REQUEST,"G002","시작 시간 값은 필수입니다."),
    HOME_TEAM_ERROR(HttpStatus.NOT_FOUND,"G003","홈 팀 값은 필수입니다."),
    AWAY_TEAM_ERROR(HttpStatus.NOT_FOUND,"G003","어웨이 팀 값은 필수입니다."),
    TEAM_DUPLICATED_ERROR(HttpStatus.BAD_REQUEST,"G004","홈/어웨이 팀은 같을 수 없습니다."),
    SCORE_ERROR(HttpStatus.BAD_REQUEST,"G005","점수는 무조건 0 이상이어야 합니다."),
    END_TIME_ERROR(HttpStatus.BAD_REQUEST,"G006","종료 시간은 시작 시간보다 이전일 수 없습니다."),
    GAME_START_ERROR(HttpStatus.BAD_REQUEST,"G007","대기 중인 경기에만 시작할 수 있습니다."),
    GAME_END_ERROR(HttpStatus.BAD_REQUEST,"G008","진행 중인 경기만 종료 가능합니다."),
    ENDED_GAME_TIME_ERROR(HttpStatus.BAD_REQUEST,"G009", "종료 상태에서는 종료 시간이 필수값입니다" ),
    GAME_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "G010", "이미 종료된 게임입니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST,"G011", "잘못된 접근입니다.")

    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
