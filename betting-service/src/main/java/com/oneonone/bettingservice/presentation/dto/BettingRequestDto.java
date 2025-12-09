package com.oneonone.bettingservice.presentation.dto;

import com.oneonone.bettingservice.domain.BetResult;
import com.oneonone.bettingservice.domain.BetType;

import java.math.BigDecimal;
import java.util.UUID;


public record  BettingRequestDto(
        UUID bettingId,
        Long userId,
        UUID gameId,
        BigDecimal betAmount,
        BigDecimal odds,
        BetType betType,
        BetResult betResult
) {}
