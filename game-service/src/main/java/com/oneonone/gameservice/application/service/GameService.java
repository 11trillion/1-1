package com.oneonone.gameservice.application.service;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.gameservice.application.event.GameCompletedEvent;
import com.oneonone.gameservice.domain.GameErrorCode;
import com.oneonone.gameservice.domain.entity.Game;
import com.oneonone.gameservice.domain.entity.GameStatus;
import com.oneonone.gameservice.infrastructure.kafka.GameEventProducer;
import com.oneonone.gameservice.infrastructure.repository.GameJPARepository;
import com.oneonone.gameservice.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {
    private final GameJPARepository gameRepository;
    private final GameEventProducer  gameEventProducer;

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

    public Page<GameResponse> getAllGames(Pageable pageable) {
        Page<Game> page = gameRepository.findAllByDeletedAtIsNull(pageable);
        return page.map(GameResponse::from);
    }

    public Game getGameById(@Valid @PathVariable UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() ->  new BusinessException(GameErrorCode.GAME_NOT_FOUND));
    }

    @Transactional
    public GameUpdateResponse updateGame(UUID gameId, GameUpdateRequest gameUpdateRequest) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->  new BusinessException(GameErrorCode.GAME_NOT_FOUND));
        //들어오는 게임의 이벤트 중복을 방지하기 위해
        GameStatus prevStatus = game.getStatus();

        game.update(
                gameUpdateRequest.homeTeam(),
                gameUpdateRequest.awayTeam(),
                gameUpdateRequest.startAt(),
                gameUpdateRequest.endAt(),
                gameUpdateRequest.homeScore(),
                gameUpdateRequest.awayScore(),
                gameUpdateRequest.status()
        );

        //game이 처음 end가 되었을 때만 kafka 이벤트 실행
        if(!prevStatus.isEnded() && game.getStatus().isEnded()) {
            GameCompletedEvent event = new GameCompletedEvent(
                    game.getGameId(),
                    game.getHomeTeam(),
                    game.getAwayTeam(),
                    game.getHomeScore(),
                    game.getAwayScore(),
                    game.getResult()
            );
            gameEventProducer.publishGameCompleted(event);
        }
        return GameUpdateResponse.from(game);
    }
    @Transactional
    public void deleteGame(UUID gameId,Long userId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->  new BusinessException(GameErrorCode.GAME_NOT_FOUND));

        game.softDelete(userId);
    }


}
