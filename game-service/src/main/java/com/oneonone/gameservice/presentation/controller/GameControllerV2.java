package com.oneonone.gameservice.presentation.controller;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.gameservice.application.command.CreateGameCommand;
import com.oneonone.gameservice.application.command.UpdateGameCommand;
import com.oneonone.gameservice.application.service.GameCQRSService;
import com.oneonone.gameservice.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/games")
@Tag(name = "게임 API", description = "CQRS 기반 게임 관련 API입니다.")
public class GameControllerV2 {
    private final GameCQRSService gameService;

    @Operation(summary = "게임 생성", description = "게임 생성 API")
    @PreAuthorize("hasRole('MASTER')")
    @PostMapping
    public ResponseEntity<ApiResponse<GameCreateResponse>> createGame
            (@Valid @RequestBody GameCreateRequest gameCreateRequest) {

        CreateGameCommand command = new CreateGameCommand(
                gameCreateRequest.homeTeam(),
                gameCreateRequest.awayTeam(),
                gameCreateRequest.startAt()
                );
        GameCreateResponse result = gameService.createGame(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result,"게임 생성이 완료되었습니다."));
    }

    @Operation(summary = "전체 게임 조회" , description = "전체 게임을 조회한다. sort 테스트 시 [] 빼고 테스트해주세요")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GameResponse>>> getAllGames(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<GameResponse> result = gameService.getAllGames(pageable);
        return ResponseEntity.ok(ApiResponse.success(result,"게임 조회 결과입니다."));

    }

    @Operation(summary = "게임 단건 조회" , description = "한 게임의 대한 정보를 조회한다.")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @GetMapping("/{gameId}")
    public ResponseEntity<ApiResponse<GameResponse>> getGameById(
            @PathVariable UUID gameId) {
        GameResponse result = GameResponse.from(gameService.getGameById(gameId));
        return ResponseEntity.ok(ApiResponse.success(result,"게임 단건조회 결과입니다."));
    }

    @Operation(summary = "게임 정보 수정", description = "게임의 정보를 수정합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{gameId}")
    public ResponseEntity<ApiResponse<GameUpdateResponse>> updateGame(
            @PathVariable UUID gameId,
            @Valid @RequestBody GameUpdateRequest gameUpdateRequest) {
        UpdateGameCommand command = new UpdateGameCommand(
                gameUpdateRequest.homeTeam(),
                gameUpdateRequest.awayTeam(),
                gameUpdateRequest.startAt(),
                gameUpdateRequest.homeScore(),
                gameUpdateRequest.awayScore(),
                gameUpdateRequest.status()
        );

        GameUpdateResponse result = gameService.updateGame(gameId,command);
        return ResponseEntity.ok(ApiResponse.success(result,"게임 정보 수정이 완료되었습니다."));
    }

    @Operation(summary = "게임 정보 삭제", description = "게임 정보를 삭제합니다")
    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/{gameId}")
    public ResponseEntity<ApiResponse<Void>> deleteGame(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable UUID gameId) {

        gameService.deleteGame(gameId,userId);
        return ResponseEntity.ok(ApiResponse.success(null,"게임 정보 삭제가 완료되었습니다."));
    }


}
