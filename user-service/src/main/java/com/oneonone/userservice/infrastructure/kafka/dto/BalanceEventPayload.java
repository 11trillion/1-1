package com.oneonone.userservice.infrastructure.kafka.dto;

public record BalanceEventPayload(
        String eventId,
        Long userId,
        Long amount,
        String type,   // CREDIT / DEBIT
        String betId     // nullable
) {
}
