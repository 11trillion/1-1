package com.oneonone.pointservice.domain.entity;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.model.BaseEntity;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.pointservice.domain.enums.PointType;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name="user_id", nullable=false)
    private Long userId;

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
            UUID eventId,
            Long userId,
            Long amount,
            PointType pointType,
            String description
    ) {
        if (amount <= 0) {
            throw new BusinessException(PointErrorCode.INVALID_AMOUNT);
        }

        Point point = new Point();
        point.eventId = eventId;
        point.userId = userId;
        point.amount = amount;
        point.pointType = pointType;
        point.description = description;
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