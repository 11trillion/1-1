package com.oneonone.pointservice.domain.entity;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.model.BaseEntity;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.common.enums.PointType;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Entity
@Getter
@Table(name = "p_points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="point_id")
    private UUID id;

    /**
     * Saga 전체 흐름 추적
     * - 일반 거래: null
     * - Saga 거래: Saga 식별자
     */
    @Column(name = "saga_id")
    private UUID sagaId;

    /**
     * 개별 이벤트 멱등성 보장
     * - Saga 거래: 이벤트 식별자 (중복 처리 방지)
     */
    @Column(name = "event_id", unique = true)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name="point_type", nullable=false)
    private PointType pointType;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private PointStatus status;

    @Column(name="amount", nullable=false)
    private Long amount;

    @Column(name="description")
    private String description;

    @Column(name = "bet_id")
    private String betId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    /**
     * 보상 완료 시각
     * - status가 COMPENSATED일 때만 값 존재
     */
    @Column(name="compensated_at")
    private LocalDateTime compensatedAt;

    // Point 생성자 (도메인 규칙 적용)
    public Point(PointType type, Long amount, String description, Long userId) {
        if (amount <= 0) {
            throw new BusinessException(PointErrorCode.INVALID_AMOUNT);
        }
        this.pointType = type;
        this.amount = amount;
        this.description = description;
        this.userId = userId;
        this.status = PointStatus.PENDING;
    }

    public static Point create(
            String sagaId,
            String eventId,
            Long userId,
            Long amount,
            PointType pointType,
            String betId
    ) {
        if (amount <= 0) {
            throw new BusinessException(PointErrorCode.INVALID_AMOUNT);
        }

        Point point = new Point();
        point.sagaId = UUID.fromString(sagaId);
        point.eventId = UUID.fromString(eventId);
        point.userId = userId;
        point.amount = amount;
        point.pointType = pointType;
        point.betId = betId;
        point.status = PointStatus.PENDING;

        return point;
    }

    public void markSuccess() {
        validateUpdatable();
        this.status = PointStatus.SUCCESS;
    }

    public void markFailed() {
        validateUpdatable();
        this.status = PointStatus.FAILED;
    }

    // 보상 시작
    public void startCompensation(String reason) {
        log.info("[POINT] startCompensation called - pointId={}, currentStatus={}, reason={}",
                this.id, this.status, reason);
        if (this.status == PointStatus.SUCCESS) {
            throw new BusinessException(PointErrorCode.ONLY_SUCCESS_CAN_BE_COMPENSATED);
        }

        this.status = PointStatus.COMPENSATING;
        this.description = reason;

        log.info("[POINT] status changed to COMPENSATING - pointId={}", this.id);
    }

    // 보상 완료
    public void markCompensated() {
        log.info(
                "[POINT] markCompensated called - pointId={}, currentStatus={}",
                this.id, this.status
        );

        if (this.status != PointStatus.COMPENSATING) {
            log.warn(
                    "[POINT] markCompensated rejected - pointId={}, status={}, expectedStatus=COMPENSATING",
                    this.id, this.status
            );
            throw new BusinessException(PointErrorCode.NOT_IN_COMPENSATING_STATUS);
        }

        this.status = PointStatus.COMPENSATED;
        this.compensatedAt = LocalDateTime.now();

        log.info(
                "[POINT] status changed - pointId={}, newStatus={}, compensatedAt={}",
                this.id, this.status, this.compensatedAt
        );
    }

    // 보상 실패 처리 (필요시)
    public void failCompensation() {
        if (this.status != PointStatus.COMPENSATING) {
            throw new BusinessException(PointErrorCode.NOT_IN_COMPENSATING_STATUS);
        }

        // COMPENSATING 상태에서 실패 시 원래 SUCCESS로 복구
        this.status = PointStatus.SUCCESS;
        this.description = null;
    }

    private void validateUpdatable(){
        if(this.status == PointStatus.SUCCESS){
            throw new BusinessException(PointErrorCode.STATUS_CANNOT_CHANGE);
        }
    }

    // todo: 이부분이 없어져야할수도 있음. 그러면 update api부분은 어떻게 처리할 것인가?
    public void changeStatus(PointStatus newStatus) {
        if (this.status == PointStatus.SUCCESS) {
            throw new BusinessException(PointErrorCode.STATUS_CANNOT_CHANGE);
        }
        this.status = newStatus;
    }
}