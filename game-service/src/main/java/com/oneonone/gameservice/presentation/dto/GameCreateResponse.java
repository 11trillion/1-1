package com.oneonone.gameservice.presentation.dto;

import com.oneonone.common.enums.GameResult;
import com.oneonone.gameservice.domain.entity.Game;
import com.oneonone.gameservice.domain.entity.GameStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameCreateResponse(
        UUID gameId,
        String homeTeam,
        String awayTeam,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int homeScore,
        int awayScore,
        GameStatus status,
        GameResult Result

) {
    public static GameCreateResponse from(Game game) {
        return new GameCreateResponse(
                game.getGameId(),
                game.getHomeTeam(),
                game.getAwayTeam(),
                game.getStartAt(),
                game.getEndAt(),
                game.getHomeScore(),
                game.getAwayScore(),
                game.getStatus(),
                game.getResult()
        );
    }
}
