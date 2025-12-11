package com.oneonone.gameservice.application.command;

import java.time.LocalDateTime;

//생성용 command
public record CreateGameCommand(
        String homeTeam,
        String awayTeam,
        LocalDateTime startAt
) {
}
