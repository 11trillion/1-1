package com.oneonone.gameservice.application.dto;

import com.oneonone.gameservice.domain.entity.GameResult;
import com.oneonone.gameservice.domain.entity.GameStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GameUpdateRequest (
        @NotBlank(message = "홈 팀은 필수입니다.")
        String homeTeam,
        @NotBlank(message = "어웨이 팀은 필수입니다.")
        String awayTeam,
        @NotNull(message = "시작 시간은 필수입니다.")
        LocalDateTime startAt,
        LocalDateTime endAt,
        @Min(value = 0, message = "홈팀 점수는 0 이상이어야 합니다.")
        int homeScore,
        @Min(value = 0, message = "어웨이 팀 점수는 0 이상이어야 합니다.")
        int awayScore,
        @NotNull(message = "경기 상태는 필수입니다.")
        GameStatus status
) {
}
