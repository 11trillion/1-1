package com.oneonone.common.infrastructure.kafka;

// Kafka message payload용
public record BalanceCompensationEvent(
        String eventId,
        Long userId,
        Long amount,
        String type,
        String betId
) {
}
