package com.oneonone.pointservice.domain.repository;

import com.oneonone.pointservice.domain.entity.Point;

import java.util.Optional;
import java.util.UUID;

public interface PointRepository {
    Point save(Point point);
    Optional<Point> findById(UUID pointId);
}
