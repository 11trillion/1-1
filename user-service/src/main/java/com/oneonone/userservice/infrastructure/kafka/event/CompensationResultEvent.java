package com.oneonone.userservice.infrastructure.kafka.event;

public record CompensationResultEvent(
        String eventId,
        boolean success,
        String failureReason
) {}