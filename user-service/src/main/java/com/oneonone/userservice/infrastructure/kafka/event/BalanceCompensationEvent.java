package com.oneonone.userservice.infrastructure.kafka.event;

import com.oneonone.common.enums.PointType;

public record BalanceCompensationEvent(
        String eventId,
        Long userId,
        Long amount,
        PointType type,
        String betId
) {
}
