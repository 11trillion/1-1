package com.oneonone.userservice.infrastructure.kafka.event;

public record BalanceCompensationEvent(
        String eventId,
        Long userId,
        Long amount,
        String type,
        String betId
) {
}
