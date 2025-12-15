package com.oneonone.bettingservice.infrastructure.client.dto;

import java.util.UUID;

public record BalanceResponse(
        Long userId,
        Long pointBalance,
        UUID sagaId
) {}
