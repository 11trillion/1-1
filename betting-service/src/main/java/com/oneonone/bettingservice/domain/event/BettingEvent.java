package com.oneonone.bettingservice.domain.event;

public record BettingEvent(
        String sagaId,
        String eventId,
        Long userId,
        Long amount,
        String betId
) {
}