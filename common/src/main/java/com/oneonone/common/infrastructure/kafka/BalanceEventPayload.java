package com.oneonone.common.infrastructure.kafka;

import com.oneonone.common.enums.PointType;

public record BalanceEventPayload(
        String eventId,
        Long userId,
        Long amount,
        PointType type,   // CREDIT / DEBIT
        String betId     // nullable
) {
}
