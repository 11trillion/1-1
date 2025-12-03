package com.oneonone.pointservice.infrastructure.kafka.dto;

import java.util.UUID;

public record BalanceEventPayload (
        Long userId,
        Long amount,
        String type,
        UUID eventId,
        String description
) {}
