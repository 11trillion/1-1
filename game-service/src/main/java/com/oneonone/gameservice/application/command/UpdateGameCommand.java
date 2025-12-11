package com.oneonone.gameservice.application.command;

import com.oneonone.gameservice.domain.entity.GameStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateGameCommand(
        UUID gameId,
        String homeTeam,
        String awayTeam,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer homeScore,
        Integer awayScore,
        GameStatus status
) {
}
