package com.oneonone.userservice.infrastructure.kafka.event;

public record BettingEvent(
        String eventId,
        Long userId,
        Long amount,
        String betId
) {
}