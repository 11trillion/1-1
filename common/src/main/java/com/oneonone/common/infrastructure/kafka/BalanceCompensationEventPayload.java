package com.oneonone.common.infrastructure.kafka;

import com.oneonone.common.enums.PointType;

// Kafka message payload용
public record BalanceCompensationEventPayload(
        String eventId,
        Long userId,
        Long amount,
        PointType type,
        String betId
) {
}
