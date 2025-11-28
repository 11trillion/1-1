package com.oneonone.gameservice.application.service;

import com.oneonone.gameservice.application.dto.GameCreateRequest;
import com.oneonone.gameservice.application.dto.GameCreateResponse;
import com.oneonone.gameservice.application.dto.GameResponse;
import com.oneonone.gameservice.domain.entity.Game;
import com.oneonone.gameservice.infrastructure.repository.GameJPARepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {
    private final GameJPARepository gameRepository;

    @Transactional
    public GameCreateResponse createGame(GameCreateRequest gameCreateRequest) {
        //User 권한 체크하는 로직 추가 필요

        //나머지 값들은 기본적으로 builder에서 넣어준다.
        Game game = Game.createGame(
                gameCreateRequest.homeTeam(),
                gameCreateRequest.awayTeam(),
                gameCreateRequest.startAt()
        );
        gameRepository.save(game);
        return GameCreateResponse.from(game);
    }

    public List<GameResponse> getAllGames() {
        List<Game> games = gameRepository.findAll();
        return games.stream().map(GameResponse::from).toList();
    }

    public Game getGameById(@Valid @PathVariable UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게임 id를 찾을 수 없습니다."));
    }

}
