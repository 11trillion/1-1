package com.oneonone.pointservice.infrastructure.kafka.event;

import com.oneonone.common.enums.PointType;

public record BalanceEvent(
        String sagaId,    // Saga correlation Id (전체 트랜잭션 추적)
        String eventId,   // Kafka 메시지 ID (개별 메시지 중복 방지)
        Long userId,
        Long amount,
        PointType type,   // CREDIT / DEBIT
        String betId,     // nullable
        Long publishedAt
) {
}
