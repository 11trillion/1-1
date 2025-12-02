package com.oneonone.gameservice.infrastructure.repository;

import com.oneonone.gameservice.domain.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameJPARepository extends JpaRepository<Game, UUID> {
}
