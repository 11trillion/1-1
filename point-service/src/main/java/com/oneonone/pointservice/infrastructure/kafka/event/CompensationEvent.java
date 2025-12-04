package com.oneonone.pointservice.infrastructure.kafka.event;

import com.oneonone.pointservice.domain.enums.PointType;

public record CompensationEvent(
        String eventId,
        Long userId,
        Long amount,
        PointType type,
        String betId,
        String reason
) {
}