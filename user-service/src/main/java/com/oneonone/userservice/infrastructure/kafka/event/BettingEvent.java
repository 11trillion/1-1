package com.oneonone.userservice.infrastructure.kafka.event;

import com.oneonone.common.enums.PointType;

import java.util.UUID;

public record BettingEvent(
        String sagaId,
        String eventId,
        Long userId,
        Long amount,
        PointType pointType,
        UUID betId
) {
}