package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.domain.entity.CompensationEvent;
import com.oneonone.userservice.domain.repository.CompensationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CompensationEventRepositoryImpl implements CompensationEventRepository{

    private final CompensationEventJpaRepository compensationEventJpaRepository;

    @Override
    public CompensationEvent save(CompensationEvent event) {
        return compensationEventJpaRepository.save(event);
    }

    @Override
    public boolean existsByOriginalEventId(UUID originalEventId) {
        return compensationEventJpaRepository.existsByOriginalEventId(originalEventId);
    }
}