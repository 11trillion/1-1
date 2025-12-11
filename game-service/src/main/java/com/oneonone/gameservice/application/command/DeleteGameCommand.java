package com.oneonone.gameservice.application.command;

import java.util.UUID;

public record DeleteGameCommand(
        UUID gameId,
        Long userId
) {
}
