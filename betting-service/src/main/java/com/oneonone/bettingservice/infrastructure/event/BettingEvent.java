package com.oneonone.bettingservice.infrastructure.event;

public record BettingEvent(
        String eventId,
        Long userId,
        Long amount,
        String betId
) {
}