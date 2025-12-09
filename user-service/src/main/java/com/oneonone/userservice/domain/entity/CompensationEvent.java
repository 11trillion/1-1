package com.oneonone.userservice.domain.entity;

import com.oneonone.common.model.BaseEntity;
import com.oneonone.common.enums.PointType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 보상 이벤트 처리 기록 테이블
 * - 보상 이벤트의 중복 처리를 방지
 * - Outbox와 구분하여 보상 이력만 추적
 */
@Entity
@Table(name = "p_compensation_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompensationEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "compensation_id")
    private UUID id;

    /**
     * 원본 이벤트 ID (Outbox의 eventId)
     * - 보상 대상이 되는 원본 트랜잭션의 식별자
     * - 중복 방지를 위한 유니크 키
     */
    @Column(name = "original_event_id", nullable = false, unique = true)
    private UUID originalEventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false)
    private PointType pointType;

    @Column(name = "compensated_at", nullable = false)
    private LocalDateTime compensatedAt;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * 보상 이벤트 생성 - 성공
     */
    public static CompensationEvent success(UUID originalEventId, Long userId, Long amount, PointType pointType) {
        CompensationEvent event = new CompensationEvent();
        event.originalEventId = originalEventId;
        event.userId = userId;
        event.amount = amount;
        event.pointType = pointType;
        event.success = true;
        event.failureReason = null;
        event.compensatedAt = LocalDateTime.now();
        return event;
    }

    /**
     * 보상 이벤트 생성 - 실패
     */
    public static CompensationEvent failure(UUID originalEventId, Long userId, Long amount, PointType pointType, String failureReason) {
        CompensationEvent event = new CompensationEvent();
        event.originalEventId = originalEventId;
        event.userId = userId;
        event.amount = amount;
        event.pointType = pointType;
        event.success = false;
        event.failureReason = failureReason;
        event.compensatedAt = LocalDateTime.now();
        return event;
    }
}
