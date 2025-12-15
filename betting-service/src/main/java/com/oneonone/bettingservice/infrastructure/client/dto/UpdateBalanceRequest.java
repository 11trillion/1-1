package com.oneonone.bettingservice.infrastructure.client.dto;

import com.oneonone.common.enums.PointType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateBalanceRequest(
        @NotNull UUID sagaId,
        @NotNull Long amount,
        @NotNull PointType type,
        UUID betId
) {}