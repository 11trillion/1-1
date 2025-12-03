package com.oneonone.pointservice.application.service;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.pointservice.domain.enums.PointType;
import com.oneonone.pointservice.domain.repository.PointRepository;
import com.oneonone.pointservice.presentation.dto.response.PointResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;

    @Transactional
    public Point createPoint(PointType type, Long amount, String description, Long userId) {
        Point point = new Point(type, amount, description, userId);
        return pointRepository.save(point);
    }

    @Transactional
    public Point updatePointStatus(UUID pointId, PointStatus status) {
        Point point = pointRepository.findById(pointId)
                .orElseThrow(() -> new BusinessException(PointErrorCode.POINT_NOT_FOUND));

        try {
            point.changeStatus(status);
        } catch (IllegalStateException e) {
            throw new BusinessException(PointErrorCode.STATUS_CANNOT_CHANGE);
        }
        return pointRepository.save(point);
    }

    public Page<PointResponse> getPoints(Long userId, String status, Pageable pageable) {
        PointStatus pointStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                pointStatus = PointStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(PointErrorCode.INVALID_STATUS);
            }
        }

        Page<Point> page = (pointStatus != null)
                ? pointRepository.findByUserIdAndStatus(userId, pointStatus, pageable)
                : pointRepository.findByUserId(userId, pageable);

        return page.map(PointResponse::from);
    }

}

