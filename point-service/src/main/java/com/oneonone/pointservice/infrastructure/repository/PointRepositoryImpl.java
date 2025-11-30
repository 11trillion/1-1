package com.oneonone.pointservice.infrastructure.repository;

import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public Point save(Point point) {
        return pointJpaRepository.save(point);
    }
}
