package com.oneonone.pointservice.infrastructure.repository;

import com.oneonone.pointservice.domain.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PointJpaRepository extends JpaRepository<Point, UUID> {
}
