package com.oneonone.gameservice.application.service;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.gameservice.application.command.CreateGameCommand;
import com.oneonone.gameservice.application.command.DeleteGameCommand;
import com.oneonone.gameservice.application.command.UpdateGameCommand;
import com.oneonone.gameservice.application.event.GameCompletedEvent;
import com.oneonone.gameservice.domain.GameErrorCode;
import com.oneonone.gameservice.domain.entity.Game;
import com.oneonone.gameservice.domain.entity.GameStatus;
import com.oneonone.gameservice.domain.repository.GameRepository;
import com.oneonone.gameservice.infrastructure.kafka.GameEventProducer;
import com.oneonone.gameservice.presentation.dto.GameCreateResponse;
import com.oneonone.gameservice.presentation.dto.GameResponse;
import com.oneonone.gameservice.presentation.dto.GameUpdateResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GameCQRSService {
    private final GameRepository gameRepository;
    private final GameEventProducer gameEventProducer;

    @Transactional
    public GameCreateResponse createGame(CreateGameCommand command) {
        Game game = Game.createGame(
                command.homeTeam(),
                command.awayTeam(),
                command.startAt()
        );
        gameRepository.save(game);

        return GameCreateResponse.from(game);
    }

    @Transactional
    public GameUpdateResponse updateGame(UpdateGameCommand command) {
        Game game = gameRepository.findById(command.gameId())
                .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND));

        GameStatus prevStatus = game.getStatus();

        //Patch기 때문에 null을 허용함, NULL일경우 기존 status 유지
        GameStatus status = (command.status() != null) ? command.status() : prevStatus;

        //END 넣어주기!
        LocalDateTime now = LocalDateTime.now();
        //CQRS에선 도메인에 넘길 때 null이 아니어야 함
        game.update(
                command.homeTeam(),
                command.awayTeam(),
                command.startAt(),
                now,
                command.homeScore(),
                command.awayScore(),
                status
        );
        if (!prevStatus.isEnded() && game.getStatus().isEnded()) {
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
    public void deleteGame(DeleteGameCommand command) {
        Game game = gameRepository.findById(command.gameId())
                .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND));

        game.softDelete(command.userId());
    }


    //---read side--
    public Page<GameResponse> getAllGames(Pageable pageable) {
        Page<Game> page = gameRepository.findAllByDeletedAtIsNull(pageable);
        return page.map(GameResponse::from);
    }

    public Game getGameById(@Valid @PathVariable UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() ->  new BusinessException(GameErrorCode.GAME_NOT_FOUND));
    }

}
