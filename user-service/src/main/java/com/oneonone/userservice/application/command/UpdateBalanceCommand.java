package com.oneonone.userservice.application.command;

import com.oneonone.common.enums.PointType;

import java.util.UUID;

public record UpdateBalanceCommand(
        UUID sagaId,
        Long amount,      // 차감/추가 금액
        PointType type,      // "DEBIT" 또는 "CREDIT"
        UUID eventId,     // 멱등성 키
        UUID betId
) {}