package com.oneonone.gameservice.application.service;

import com.oneonone.gameservice.application.dto.GameCreateRequest;
import com.oneonone.gameservice.application.dto.GameCreateResponse;
import com.oneonone.gameservice.domain.entity.Game;
import com.oneonone.gameservice.infrastructure.repository.GameJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {
    private final GameJPARepository gameJPARepository;

    public GameCreateResponse createGame(GameCreateRequest gameCreateRequest) {
        //User 권한 체크하는 로직 추가 필요

        //나머지 값들은 기본적으로 builder에서 넣어준다.
        Game game = Game.createGame(
                gameCreateRequest.homeTeam(),
                gameCreateRequest.awayTeam(),
                gameCreateRequest.startAt()
        );
        gameJPARepository.save(game);
        return GameCreateResponse.from(game);
    }

}
