package com.oneonone.userservice.application.command;

import java.util.UUID;

public record UpdateBalanceCommand(
        Long amount,      // 차감/추가 금액
        String type,      // "DEBIT" 또는 "CREDIT"
        UUID eventId,     // 멱등성 키
        UUID betId
) {}