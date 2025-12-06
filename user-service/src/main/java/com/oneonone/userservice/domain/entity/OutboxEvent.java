package com.oneonone.userservice.domain.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.enums.OutboxStatus;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceEvent;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_outboxes")
@Getter
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID outboxId;

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private Integer retryCount = 0;

    protected OutboxEvent() {}

    public OutboxEvent(UUID eventId, Long userId, String payload) {
        this.eventId = eventId;
        this.userId = userId;
        this.payload = payload;
    }

    @PrePersist
    protected void create() {
        this.createdAt = LocalDateTime.now();
    }

    public void markAsSuccess() {
        this.status = OutboxStatus.SUCCESS;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
    }

    public void increaseRetry() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return this.status == OutboxStatus.PENDING && this.retryCount < 3;
    }


    /**
     * JSON payload를 BalanceEventPayload DTO로 변환
     */
    public BalanceEvent toBalanceEventPayload(ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(this.payload, BalanceEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("OutboxEvent JSON -> DTO 변환 실패", e);
        }
    }
}
