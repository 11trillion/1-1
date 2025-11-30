package com.oneonone.pointservice.domain.repository;

import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointRepository {
    Point save(Point point);
    Optional<Point> findById(UUID pointId);
    List<Point> findByUserId(Long userId);
    List<Point> findByUserIdAndStatus(Long userId, PointStatus pointStatus);
}
