package com.oneonone.userservice.infrastructure.kafka.event;

import com.oneonone.common.enums.PointType;

public record BettingEvent(
        String eventId,
        Long userId,
        Long amount,
        PointType type,
        String betId
) {
}