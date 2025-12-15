package com.oneonone.gameservice.presentation.dto;

import com.oneonone.gameservice.domain.entity.GameStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GameUpdateRequest (
        String homeTeam,
        String awayTeam,
        LocalDateTime startAt,
        //LocalDateTime endAt, => now로 사용
        @Min(value = 0, message = "홈팀 점수는 0 이상이어야 합니다.")
        Integer homeScore,
        @Min(value = 0, message = "어웨이 팀 점수는 0 이상이어야 합니다.")
        Integer awayScore,
        @NotNull(message = "경기 상태는 필수입니다.")
        GameStatus status
) {
}
