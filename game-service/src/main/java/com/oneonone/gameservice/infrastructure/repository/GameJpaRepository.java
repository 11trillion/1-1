package com.oneonone.gameservice.infrastructure.repository;

import com.oneonone.gameservice.domain.entity.Game;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GameJpaRepository extends JpaRepository<Game, UUID> {
    Page<Game> findAllByDeletedAtIsNull(Pageable pageable);
    Optional<Game> findByGameIdAndDeletedAtIsNull(UUID gameId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Game g where g.gameId= :gameId and g.deletedAt is null")
    Optional<Game> findByGameIdForUpdate(@Param("gameId") UUID gameId);
}
