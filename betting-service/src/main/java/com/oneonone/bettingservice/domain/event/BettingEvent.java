package com.oneonone.bettingservice.domain.event;

public record BettingEvent(
        String eventId,
        Long userId,
        Long amount,
        String betId
) {
}