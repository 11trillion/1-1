package com.oneonone.userservice.infrastructure.kafka.event;

import com.oneonone.common.enums.PointType;

public record BalanceEvent (
        String sagaId, //Saga correlation Id
        String eventId, // Kafka 메시지 ID (UUID.randomID())
        Long userId,
        Long amount,
        PointType type,   // CREDIT / DEBIT
        String betId,    // nullable
        Long publishedAt
) {
}
