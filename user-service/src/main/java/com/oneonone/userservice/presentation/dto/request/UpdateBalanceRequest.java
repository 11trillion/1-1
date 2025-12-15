package com.oneonone.userservice.presentation.dto.request;

import com.oneonone.common.enums.PointType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateBalanceRequest(
        @NotNull(message = "금액은 필수입니다")
        Long amount,

        @NotNull(message = "타입은 필수입니다")
        PointType type,  // "DEBIT" or "CREDIT"

        UUID sagaId,

        // betId는 선택적 (베팅 관련 작업에만 필요)
        UUID betId
) {}
