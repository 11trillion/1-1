package com.oneonone.pointservice.infrastructure.kafka.event;

public record CompensationResultEvent(
        String eventId,
        boolean success,
        String failureReason
) {}