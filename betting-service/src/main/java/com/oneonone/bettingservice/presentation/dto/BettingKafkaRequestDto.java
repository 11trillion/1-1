package com.oneonone.bettingservice.presentation.dto;

import com.oneonone.bettingservice.domain.BetType;

import java.util.UUID;

public record BettingKafkaRequestDto(
        UUID gameId,
        BetType gameResult
)
{}
