package com.oneonone.userservice.presentation.dto.request;

import java.util.UUID;

public record UpdateBalanceRequest(
        Long amount,      // 차감/추가 금액
        String type,      // "DEBIT" 또는 "CREDIT"
        UUID eventId      // 멱등성 키
) {}
