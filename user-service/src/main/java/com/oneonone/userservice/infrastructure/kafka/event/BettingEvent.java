package com.oneonone.userservice.infrastructure.kafka.event;

public record BettingEvent(
        String sagaId,
        String eventId,
        Long userId,
        Long amount,
        String betId
) {
}