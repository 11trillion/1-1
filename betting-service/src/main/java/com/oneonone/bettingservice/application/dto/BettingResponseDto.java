package com.oneonone.bettingservice.application.dto;

import com.oneonone.bettingservice.domain.vo.BetResult;
import com.oneonone.bettingservice.domain.entity.Betting;
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
        BetResult betResult,
        Long remainingPoint
){
    /**
     * 베팅 생성 시 사용 (포인트 정보 포함)
     */
    public static BettingResponseDto from(Betting betting, Long remainingPoint) {
        return new BettingResponseDto(
                betting.getId(),
                betting.getUserId(),
                betting.getGameId(),
                betting.getBetAmount(),
                betting.getOdds(),
                betting.getBetType(),
                betting.getBetResult(),
                remainingPoint
        );
    }


    /**
     * 조회 시 사용 (포인트 정보 없음)
     */
    public static BettingResponseDto from(Betting betting) {
        return new BettingResponseDto(
                betting.getId(),
                betting.getUserId(),
                betting.getGameId(),
                betting.getBetAmount(),
                betting.getOdds(),
                betting.getBetType(),
                betting.getBetResult(),
                null
        );
    }
}
