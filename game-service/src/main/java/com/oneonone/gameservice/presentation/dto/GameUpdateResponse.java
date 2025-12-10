package com.oneonone.gameservice.presentation.dto;

import com.oneonone.common.enums.GameResult;
import com.oneonone.gameservice.domain.entity.Game;
import com.oneonone.gameservice.domain.entity.GameStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameUpdateResponse (
        UUID gameId,
        String homeTeam,
        String awayTeam,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int homeScore,
        int awayScore,
        GameStatus status,
        GameResult result
) {
    public static GameUpdateResponse from(Game game) {
        return new GameUpdateResponse(
                game.getGameId(),
                game.getHomeTeam(),
                game.getAwayTeam(),
                game.getStartAt(),
                game.getEndAt(),
                game.getHomeScore(),
                game.getAwayScore(),
                game.getStatus(),
                game.getResult());
    }
}
