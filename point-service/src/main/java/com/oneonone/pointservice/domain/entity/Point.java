package com.oneonone.pointservice.domain.entity;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.model.BaseEntity;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.common.enums.PointType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="point_id")
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;   // Saga / Kafka 멱등성 키

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

    private String betId;

    @Column(name="user_id", nullable=false)
    private Long userId;

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
        if (this.status == PointStatus.SUCCESS) {
            throw new BusinessException(PointErrorCode.ONLY_SUCCESS_CAN_BE_COMPENSATED);
        }

        this.status = PointStatus.COMPENSATING;
        this.description = reason;
    }

    // 보상 완료
    public void markCompensated() {
        if (this.status != PointStatus.COMPENSATING) {
            throw new BusinessException(PointErrorCode.NOT_IN_COMPENSATING_STATUS);
        }

        this.status = PointStatus.COMPENSATED;
        this.compensatedAt = LocalDateTime.now();
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