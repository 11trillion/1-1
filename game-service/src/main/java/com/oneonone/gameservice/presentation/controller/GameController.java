package com.oneonone.gameservice.presentation.controller;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.gameservice.application.dto.GameCreateRequest;
import com.oneonone.gameservice.application.dto.GameCreateResponse;
import com.oneonone.gameservice.application.dto.GameResponse;
import com.oneonone.gameservice.application.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
@Tag(name = "게임 API", description = "게임 관련 API입니다.")
public class GameController {
    private final GameService gameService;

    @Operation(summary = "게임 생성", description = "게임 생성 API")
    @PostMapping
    public ResponseEntity<ApiResponse<GameCreateResponse>> createGame
            (@Valid @RequestBody GameCreateRequest gameCreateRequest) {
        GameCreateResponse result = gameService.createGame(gameCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result,"게임 생성이 완료되었습니다."));
    }

    @Operation(summary = "전체 게임 조회" , description = "전체 게임을 조회한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GameResponse>>> getAllGames() {
        List<GameResponse> list = gameService.getAllGames();
        return ResponseEntity.ok(ApiResponse.success(list,"게임 조회 결과입니다."));

    }

    @Operation(summary = "게임 단건 조회" , description = "한 게임의 대한 정보를 조회한다.")
    @GetMapping("/{gameId}")
    public ResponseEntity<ApiResponse<GameResponse>> getGameById(@Valid @PathVariable UUID gameId) {
        GameResponse result = GameResponse.from(gameService.getGameById(gameId));
        return ResponseEntity.ok(ApiResponse.success(result,"게임 단건조회 결과입니다."));
    }
}
