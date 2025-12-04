package com.oneonone.userservice.domain.repository;

import com.oneonone.userservice.domain.entity.OutboxEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxRepository {

    OutboxEvent save(OutboxEvent event);

    boolean existsByEventId(UUID eventId);

    List<OutboxEvent> findUnprocessedEvents(int limit);

    Optional<OutboxEvent> findByEventId(UUID eventId);
}
