package com.oneonone.pointservice.infrastructure.kafka.event;

import com.oneonone.common.enums.PointType;

public record BalanceEvent(
        String eventId,
        Long userId,
        Long amount,
        PointType type,   // CREDIT / DEBIT
        String betId     // nullable
) {
}
