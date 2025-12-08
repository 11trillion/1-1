package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.domain.entity.CompensationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompensationEventJpaRepository extends JpaRepository<CompensationEvent, UUID> {

    /**
     * 해당 원본 이벤트 ID에 대한 보상이 이미 처리되었는지 확인
     */
    boolean existsByOriginalEventId(UUID originalEventId);
}