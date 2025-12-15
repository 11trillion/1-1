package com.oneonone.bettingservice.application.dto;

import com.oneonone.common.enums.GameResult;

import java.math.BigDecimal;
import java.util.UUID;


public record  BettingRequestDto(
        UUID gameId,
        BigDecimal betAmount,
        BigDecimal odds,
        GameResult betType
) {}
