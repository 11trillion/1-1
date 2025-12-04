package com.oneonone.gameservice.application.event;

import com.oneonone.gameservice.domain.entity.GameResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameCompletedEvent(
        UUID gameId,
        String homeTeam,
        String awayTeam,
        int homeScore,
        int awayScore,
        GameResult gameResult
) {
}
