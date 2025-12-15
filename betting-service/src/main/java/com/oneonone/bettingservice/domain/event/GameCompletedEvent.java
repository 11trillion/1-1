package com.oneonone.bettingservice.domain.event;

import com.oneonone.common.enums.GameResult;

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
