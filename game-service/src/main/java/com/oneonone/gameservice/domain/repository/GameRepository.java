package com.oneonone.gameservice.domain.repository;

import com.oneonone.gameservice.domain.entity.Game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository {
    Game save(Game game);
    Optional<Game> findById(UUID id);
    List<Game> findAll();


}
