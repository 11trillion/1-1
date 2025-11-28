package com.oneonone.gameservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GameCreateRequest(
        @NotBlank(message = "홈팀 이름은 필수입니다.")
        String homeTeam,
        @NotBlank(message = "원정팀 이름은 필수입니다.")
        String awayTeam,
        @NotNull(message = "경기 시작 시간은 필수입니다.")
        LocalDateTime startAt
) {

}
