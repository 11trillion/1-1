package com.oneonone.gameservice.application.command;

import com.oneonone.gameservice.domain.entity.GameStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateGameCommand(
        String homeTeam,
        String awayTeam,
        LocalDateTime startAt,
        //LocalDateTime endAt, => now라 필요없음
        Integer homeScore,
        Integer awayScore,
        GameStatus status
) {
}
