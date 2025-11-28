package com.oneonone.bettingservice.presentation;

import com.oneonone.bettingservice.application.dto.BettingRequestDto;
import com.oneonone.bettingservice.application.dto.BettingResponseDto;
import com.oneonone.bettingservice.application.service.BettingService;
import com.oneonone.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bets")
public class BettingController {

    private final BettingService bettingService;

    // 베팅 내역 조회_베팅ID
    @GetMapping("/{betId}")
    public ApiResponse<Page<BettingResponseDto>> getBetListByBetId(
            @PathVariable UUID betId,
            @RequestParam(value = "page", defaultValue = "0") int page, // 페이지 번호
            @RequestParam(value = "size", defaultValue = "10") int size, // 조회할 항목수
            @RequestParam(value = "sort", defaultValue = "createdAt") String sort // 정렬기준
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByBetId(betId, page, size, sort);
        return ApiResponse.success(result, "베팅 조회 성공");
    }

    // 베팅 내역 조회_게임ID
    @GetMapping("/game/{gameId}")
    public ApiResponse<Page<BettingResponseDto>> getBetListByGameId(
            @PathVariable UUID gameId,
            @RequestParam(value = "page", defaultValue = "0") int page, // 페이지 번호
            @RequestParam(value = "size", defaultValue = "10") int size, // 조회할 항목수
            @RequestParam(value = "sort", defaultValue = "createdAt") String sort // 정렬기준
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByGameId(gameId, page, size, sort);
        return ApiResponse.success(result, "베팅 조회 성공");
    }

    // 베팅 내역 조회_사용자ID
    @GetMapping("/user/{userId}")
    public ApiResponse<Page<BettingResponseDto>> getBetListByUserId(
            @PathVariable Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page, // 페이지 번호
            @RequestParam(value = "size", defaultValue = "10") int size, // 조회할 항목수
            @RequestParam(value = "sort", defaultValue = "createdAt") String sort // 정렬기준
    ){
        Page<BettingResponseDto> result = bettingService.getBetListByUserId(userId, page, size, sort);
        return ApiResponse.success(result, "베팅 조회 성공");
    }

    // 베팅 생성
    @PostMapping
    public ApiResponse<BettingResponseDto> createBetting(
            @RequestBody BettingRequestDto requestDto
    ){
        BettingResponseDto result = bettingService.createBetting(requestDto);
        return ApiResponse.success(result, "베팅 생성 성공");
    }

    // 베팅 수정
    @PatchMapping("/{betId}")
    public ApiResponse<BettingResponseDto> updateBetting(
            @RequestBody BettingRequestDto requestDto
    ){
        BettingResponseDto result = bettingService.updateBetting(requestDto);
        return ApiResponse.success(result, "베팅 수정 성공");
    }

    // 베팅 삭제
    @DeleteMapping("/{betId}")
    public ApiResponse<BettingResponseDto> deleteBetting(
            @PathVariable UUID betId
    ){
        //임시 처리
        Long userId = 999999L;
        bettingService.deleteBetting(betId, userId);
        return ApiResponse.success("베팅 삭제 성공");
    }

}
