package com.oneonone.pointservice.application;

import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointType;
import com.oneonone.pointservice.domain.repository.PointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceV1 {
    private final PointRepository pointRepository;

    @Transactional
    public Point createPoint(PointType type, int amount, String description, Long userId) {
        Point point = new Point(type, amount, description, userId);
        return pointRepository.save(point);
    }
}

