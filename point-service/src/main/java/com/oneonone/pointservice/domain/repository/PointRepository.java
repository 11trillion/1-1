package com.oneonone.pointservice.domain.repository;

import com.oneonone.pointservice.domain.entity.Point;

public interface PointRepository {
    Point save(Point point);
}
