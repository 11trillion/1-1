package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.enums.OutboxStatus;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return outboxJpaRepository.save(event);
    }

    @Override
    public boolean existsByEventId(UUID eventId) {
        return outboxJpaRepository.existsByEventId(eventId);
    }

    @Override
    public Optional<OutboxEvent> findByEventId(UUID eventId) {
        return outboxJpaRepository.findByEventId(eventId);
    }

    @Override
    public List<OutboxEvent> findUnprocessedEvents(int limit) {
        // limit은 일단 100으로 고정 (추후 파라미터화 가능)
        return outboxJpaRepository.findTop100ByStatusOrderByOutboxIdAsc(OutboxStatus.PENDING);
    }
}