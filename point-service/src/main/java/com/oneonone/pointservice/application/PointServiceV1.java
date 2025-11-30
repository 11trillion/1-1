package com.oneonone.pointservice.application;

import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.pointservice.domain.enums.PointType;
import com.oneonone.pointservice.domain.repository.PointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointServiceV1 {
    private final PointRepository pointRepository;

    @Transactional
    public Point createPoint(PointType type, int amount, String description, Long userId) {
        Point point = new Point(type, amount, description, userId);
        return pointRepository.save(point);
    }

    @Transactional
    public Point updatePointStatus(UUID pointId, PointStatus status) {
        Point point = pointRepository.findById(pointId)
                .orElseThrow(() -> new IllegalArgumentException(PointErrorCode.POINT_NOT_FOUND.getMessage() + pointId));

        try {
            point.changeStatus(status);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(PointErrorCode.STATUS_CANNOT_CHANGE.getMessage());
        }
        return pointRepository.save(point);
    }
}

