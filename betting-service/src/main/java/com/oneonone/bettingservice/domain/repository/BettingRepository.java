package com.oneonone.bettingservice.domain.repository;

import com.oneonone.bettingservice.domain.entity.Betting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BettingRepository extends JpaRepository<Betting, UUID> {


    Page<Betting> findAllById(UUID id, Pageable pageable);

    Page<Betting> findAllByGameId(UUID gameId, Pageable pageable);

    Page<Betting> findAllByUserId(Long userId, Pageable pageable);

    Optional<List<Betting>> findAllByGameId(UUID gameId);
}
