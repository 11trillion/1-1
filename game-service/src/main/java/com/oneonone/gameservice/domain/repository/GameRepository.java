package com.oneonone.gameservice.domain.repository;

import com.oneonone.gameservice.domain.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository {
    Game save(Game game);
    Optional<Game> findById(UUID id);
    Page<Game> findAllByDeletedAtIsNull(Pageable pageable);
    Optional<Game> findByGameIdAndDeletedAtIsNull(UUID gameId);
    Optional<Game> findByGameIdForUpdate(UUID gameId);
}
