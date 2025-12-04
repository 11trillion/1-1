package com.oneonone.pointservice.infrastructure.kafka.dto;

import com.oneonone.pointservice.domain.enums.PointType;

public record BalanceEventPayload (
        String eventId,
        Long userId,
        Long amount,
        PointType type,   // CREDIT / DEBIT
        String betId     // nullable
) {}

