package com.oneonone.bettingservice.infrastructure.client.dto;

import com.oneonone.bettingservice.domain.vo.GameStatus;
import com.oneonone.common.enums.GameResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameResponse (
        UUID gameId,
        String homeTeam,
        String awayTeam,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int homeScore,
        int awayScore,
        GameStatus status,
        GameResult Result
){
}
