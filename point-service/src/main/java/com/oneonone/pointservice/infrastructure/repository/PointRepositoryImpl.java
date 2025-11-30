package com.oneonone.pointservice.infrastructure.repository;

import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.pointservice.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public Point save(Point point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public Optional<Point> findById(UUID pointId) {
        return pointJpaRepository.findById(pointId);
    }

    @Override
    public Page<Point> findByUserId(Long userId, Pageable pageable) {
        return pointJpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Point> findByUserIdAndStatus(Long userId, PointStatus pointStatus, Pageable pageable) {
        return pointJpaRepository.findByUserIdAndStatus(userId, pointStatus, pageable);
    }
}
