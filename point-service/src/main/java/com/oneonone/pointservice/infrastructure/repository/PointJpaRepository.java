package com.oneonone.pointservice.infrastructure.repository;

import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PointJpaRepository extends JpaRepository<Point, UUID> {
    List<Point> findByUserId(Long userId);
    List<Point> findByUserIdAndStatus(Long userId, PointStatus status);
}
