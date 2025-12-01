package com.oneonone.bettingservice.application.dto;

import com.oneonone.bettingservice.domain.BetResult;
import com.oneonone.bettingservice.domain.BetType;

import java.math.BigDecimal;
import java.util.UUID;

public record  BettingRequestDto(
        UUID bettingId,
        Long userId,
        UUID gameId,
        Integer betAmount,
        BigDecimal odds,
        BetType betType,
        BetResult betResult
) {}
