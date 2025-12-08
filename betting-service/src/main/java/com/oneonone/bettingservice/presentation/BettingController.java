package com.oneonone.bettingservice.presentation;

import com.oneonone.bettingservice.application.service.BettingService;
import com.oneonone.bettingservice.presentation.dto.BettingRequestDto;
import com.oneonone.bettingservice.presentation.dto.BettingResponseDto;
import com.oneonone.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "베팅", description = "베팅 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bets")
public class BettingController {

    private final BettingService bettingService;

    // 베팅 내역 조회_베팅ID
    @Operation(summary = "베팅 내역 조회_베팅Id" , description = "베팅내역을 베팅ID로 조회합니다.")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @GetMapping("/{betId}")
    public ResponseEntity<ApiResponse<Page<BettingResponseDto>>> getBetListByBetId(
            @PathVariable UUID betId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByBetId(betId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 조회 성공"));
    }

    // 베팅 내역 조회_게임ID
    @Operation(summary = "베팅 내역 조회_게임Id" , description = "베팅내역을 게임ID로 조회합니다.")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @GetMapping("/game/{gameId}")
    public ResponseEntity<ApiResponse<Page<BettingResponseDto>>> getBetListByGameId(
            @PathVariable UUID gameId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByGameId(gameId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 조회 성공"));
    }

    // 베팅 내역 조회_사용자ID
    @Operation(summary = "베팅 내역 조회_사용자Id" , description = "베팅내역을 사용자ID로 조회합니다.")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<BettingResponseDto>>> getBetListByUserId(
            @PathVariable Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 조회 성공"));
    }

    // 베팅 생성
    @Operation(summary = "베팅 생성." , description = "베팅을 생성합니다.")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @PostMapping
    public ResponseEntity<ApiResponse<BettingResponseDto>> createBetting(
            @RequestBody BettingRequestDto requestDto
    ){
        BettingResponseDto result = bettingService.createBetting(requestDto);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 생성 성공"));
    }

    // 베팅 수정
    @Operation(summary = "베팅 수정" , description = "베팅을 수정합니다.")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @PatchMapping("/{betId}")
    public ResponseEntity<ApiResponse<BettingResponseDto>> updateBetting(
            @RequestBody BettingRequestDto requestDto
    ){
        BettingResponseDto result = bettingService.updateBetting(requestDto);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 수정 성공"));
    }

    // 베팅 삭제
    @Operation(summary = "베팅 삭제" , description = "베팅을 삭제합니다.")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @DeleteMapping("/{betId}")
    public ResponseEntity<ApiResponse<BettingResponseDto>> deleteBetting(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable UUID betId
    ){
        bettingService.deleteBetting(betId, userId);
        return ResponseEntity.ok(ApiResponse.success("베팅 삭제 성공"));
    }

    // todo kafka 테스트 - 추후 삭제 예정
    @GetMapping("/kafkaTest")
    public String kafkaTest(){
        return bettingService.kafkaTest();
    }
}
