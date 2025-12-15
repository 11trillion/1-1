package com.oneonone.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_processed_betting_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedBettingEvent {
    @Id
    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false, unique = true)
    private UUID betId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    public static ProcessedBettingEvent of(UUID eventId, Long userId, UUID betId, Long amount) {
        ProcessedBettingEvent event = new ProcessedBettingEvent();
        event.eventId = eventId;
        event.userId = userId;
        event.betId = betId;
        event.amount = amount;
        event.processedAt = LocalDateTime.now();
        return event;
    }
}