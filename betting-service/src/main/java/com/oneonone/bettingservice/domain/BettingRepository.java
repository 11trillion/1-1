package com.oneonone.bettingservice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BettingRepository extends JpaRepository<Betting, UUID> {


    Page<Betting> findAllById(UUID id, Pageable pageable);

    Page<Betting> findAllByGameId(UUID gameId, Pageable pageable);

    Page<Betting> findAllByUserId(Long userId, Pageable pageable);
}
