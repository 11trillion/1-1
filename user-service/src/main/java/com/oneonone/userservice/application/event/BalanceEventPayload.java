package com.oneonone.userservice.application.event;

import com.oneonone.common.enums.PointType;

public record BalanceEventPayload (
        String sagaId,
        String eventId,
        Long userId,
        Long amount,
        PointType type,
        String betId
) {
}
