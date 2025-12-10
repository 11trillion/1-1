package com.oneonone.bettingservice.presentation.dto;

import com.oneonone.bettingservice.domain.BetResult;
import com.oneonone.bettingservice.domain.Betting;
import com.oneonone.common.enums.GameResult;

import java.math.BigDecimal;
import java.util.UUID;

public record BettingResponseDto (
        UUID bettingId,
        Long userId,
        UUID gameId,
        BigDecimal betAmount,
        BigDecimal odds,
        GameResult betType,
        BetResult betResult
){
    public static BettingResponseDto from (Betting betting){
        return new BettingResponseDto (
                betting.getId(),
                betting.getUserId(),
                betting.getGameId(),
                betting.getBetAmount(),
                betting.getOdds(),
                betting.getBetType(),
                betting.getBetResult());
    }
}
