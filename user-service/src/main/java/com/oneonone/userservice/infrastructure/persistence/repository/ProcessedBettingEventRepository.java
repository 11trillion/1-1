package com.oneonone.userservice.infrastructure.persistence.repository;

import com.oneonone.userservice.infrastructure.persistence.entity.ProcessedBettingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedBettingEventRepository extends JpaRepository<ProcessedBettingEvent, UUID> {
    boolean existsByEventId(UUID eventId);

    boolean existsByBetId(UUID betId);
}
