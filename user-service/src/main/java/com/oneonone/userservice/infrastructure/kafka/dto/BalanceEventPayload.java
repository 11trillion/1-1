package com.oneonone.userservice.infrastructure.kafka.dto;

import java.util.UUID;

public record BalanceEventPayload(
        UUID eventId,
        Long userId,
        Long amount,
        String type,   // CREDIT / DEBIT
        Long betId     // nullable
) {
}
