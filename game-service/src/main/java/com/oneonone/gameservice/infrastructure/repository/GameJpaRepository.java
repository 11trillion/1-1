package com.oneonone.gameservice.infrastructure.repository;

import com.oneonone.gameservice.domain.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameJpaRepository extends JpaRepository<Game, UUID> {
    Page<Game> findAllByDeletedAtIsNull(Pageable pageable);
    Optional<Game> findByGameIdAndDeletedAtIsNull(UUID gameId);
}
