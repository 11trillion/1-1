package com.oneonone.pointservice.domain.repository;

import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointRepository {
    Point save(Point point);
    Optional<Point> findById(UUID pointId);
    Page<Point> findByUserId(Long userId, Pageable pageable);
    Page<Point> findByUserIdAndStatus(Long userId, PointStatus status, Pageable pageable);
    // 멱등성 체크 (eventId 기반)
    boolean existsByEventId(UUID eventId);
    Optional<Point> findByEventId(UUID eventId);
    // Saga 추적 (sagaId 기반)
    List<Point> findBySagaId(UUID sagaId);

    boolean existsByBetId(String betId);
}
