package com.oneonone.gameservice.infrastructure.repository;

import com.oneonone.gameservice.domain.entity.Game;
import com.oneonone.gameservice.domain.repository.GameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
@AllArgsConstructor
public class GameRepositoryImpl implements GameRepository {
    private final GameJPARepository gameJPARepository;

    @Override
    public Game save(Game game) {
        return gameJPARepository.save(game);
    }

    @Override
    public Optional<Game> findById(UUID gameId) {
        return gameJPARepository.findById(gameId);
    }

    @Override
    public List<Game> findAll() {
        return gameJPARepository.findAll();
    }
}
