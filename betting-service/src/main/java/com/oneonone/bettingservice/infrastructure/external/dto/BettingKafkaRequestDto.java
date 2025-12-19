package com.oneonone.bettingservice.infrastructure.external.dto;

import com.oneonone.common.enums.GameResult;

import java.util.UUID;

public record BettingKafkaRequestDto(
        UUID gameId,
        GameResult gameResult
)
{}
