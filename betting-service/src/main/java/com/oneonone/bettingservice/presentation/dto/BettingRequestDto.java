package com.oneonone.bettingservice.presentation.dto;

import com.oneonone.common.enums.GameResult;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;


public record  BettingRequestDto(
        @NotNull(message = "게임 ID는 필수입니다")
        UUID gameId,

        @NotNull(message = "베팅 금액은 필수입니다")
        @DecimalMin(value = "100", message = "최소 베팅 금액은 100원입니다")
        @DecimalMax(value = "1000000", message = "최대 베팅 금액은 100만원입니다")
        BigDecimal betAmount,

        @NotNull(message = "배당률은 필수입니다")
        @DecimalMin(value = "1.01", message = "배당률은 1.01 이상이어야 합니다")
        BigDecimal odds,

        @NotNull(message = "베팅 타입은 필수입니다")
        GameResult betType
) {}
