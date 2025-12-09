package com.oneonone.pointservice.infrastructure.repository;

import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointJpaRepository extends JpaRepository<Point, UUID> {
    Page<Point> findByUserId(Long userId, Pageable pageable);
    Page<Point> findByUserIdAndStatus(Long userId, PointStatus status, Pageable pageable);
    boolean existsByEventId(UUID eventId);
    Optional<Point> findByEventId(UUID eventId);
    List<Point> findBySagaId(UUID sagaId);
    boolean existsByBetId(String betId);
}
