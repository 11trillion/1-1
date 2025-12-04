package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.common.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    boolean existsByEventId(UUID eventId);

    Optional<OutboxEvent> findByEventId(UUID eventId);

    List<OutboxEvent> findTop100ByStatusOrderByOutboxIdAsc(OutboxStatus status);
}