package com.oneonone.bettingservice.presentation;

import com.oneonone.bettingservice.presentation.dto.BettingRequestDto;
import com.oneonone.bettingservice.presentation.dto.BettingResponseDto;
import com.oneonone.bettingservice.application.service.BettingService;
import com.oneonone.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bets")
public class BettingController {

    private final BettingService bettingService;

    // 베팅 내역 조회_베팅ID
    @GetMapping("/{betId}")
    public ResponseEntity<ApiResponse<Page<BettingResponseDto>>> getBetListByBetId(
            @PathVariable UUID betId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByBetId(betId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 조회 성공"));
    }

    // 베팅 내역 조회_게임ID
    @GetMapping("/game/{gameId}")
    public ResponseEntity<ApiResponse<Page<BettingResponseDto>>> getBetListByGameId(
            @PathVariable UUID gameId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByGameId(gameId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 조회 성공"));
    }

    // 베팅 내역 조회_사용자ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<BettingResponseDto>>> getBetListByUserId(
            @PathVariable Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 조회 성공"));
    }

    // 베팅 생성
    @PostMapping
    public ResponseEntity<ApiResponse<BettingResponseDto>> createBetting(
            @RequestBody BettingRequestDto requestDto
    ){
        BettingResponseDto result = bettingService.createBetting(requestDto);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 생성 성공"));
    }

    // 베팅 수정
    @PatchMapping("/{betId}")
    public ResponseEntity<ApiResponse<BettingResponseDto>> updateBetting(
            @RequestBody BettingRequestDto requestDto
    ){
        BettingResponseDto result = bettingService.updateBetting(requestDto);
        return ResponseEntity.ok(ApiResponse.success(result, "베팅 수정 성공"));
    }

    // 베팅 삭제
    @DeleteMapping("/{betId}")
    public ResponseEntity<ApiResponse<BettingResponseDto>> deleteBetting(
            @PathVariable UUID betId
    ){
        //임시 처리
        Long userId = 999999L;
        bettingService.deleteBetting(betId, userId);
        return ResponseEntity.ok(ApiResponse.success("베팅 삭제 성공"));
    }

}
