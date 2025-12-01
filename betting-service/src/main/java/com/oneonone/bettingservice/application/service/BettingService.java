package com.oneonone.bettingservice.application.service;

import com.oneonone.bettingservice.application.dto.BettingRequestDto;
import com.oneonone.bettingservice.application.dto.BettingResponseDto;
import com.oneonone.bettingservice.domain.Betting;
import com.oneonone.bettingservice.domain.BettingErrorCode;
import com.oneonone.bettingservice.domain.BettingRepository;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BettingService {
    private final BettingRepository bettingRepository;

    // 베팅 내역 조회
    public Betting betting (UUID bettingId){
        return bettingRepository
                .findById(bettingId)
                .orElseThrow(()->new BusinessException(BettingErrorCode.BETTING_NOT_FOUND));
    }

    // 베팅내역 조회_베팅
    public Page<BettingResponseDto> getBetListByBetId(
            UUID betId, Pageable pageable
    ){
        // 1. Repository에서 페이지 & 정렬하여 배달 목록 조회
        Page<Betting> bettingPage = bettingRepository.findAllById(betId, pageable);

        // 2. Entity -> DTO 반환
        return bettingPage.map(BettingResponseDto::from);
    }

    // 베팅내역 조회_게임
    public Page<BettingResponseDto> getBetListByGameId(
            UUID gameId, Pageable pageable
    ){
        // 1. Repository에서 페이지 & 정렬하여 배달 목록 조회
        Page<Betting> bettingPage = bettingRepository.findAllByGameId(gameId, pageable);

        // 2. Entity -> DTO 반환
        return bettingPage.map(BettingResponseDto::from);
    }

    // 베팅내역 조회_유저
    public Page<BettingResponseDto> getBetListByUserId(
            Long userId, Pageable pageable
    ){
        // 1. Repository에서 페이지 & 정렬하여 배달 목록 조회
        Page<Betting> bettingPage = bettingRepository.findAllByUserId(userId, pageable);

        // 2. Entity -> DTO 반환
        return bettingPage.map(BettingResponseDto::from);
    }

    // 베팅 생성
    public BettingResponseDto createBetting(BettingRequestDto requestDto){
        // 생성
        Betting betting = Betting.createBetting(
                requestDto.userId(),
                requestDto.gameId(),
                requestDto.betAmount(),
                requestDto.odds(),
                requestDto.betType(),
                requestDto.betResult()
        );

        // 저장
        bettingRepository.save(betting);

        return  BettingResponseDto.from(betting);
    }

    // 베팅 수정
    public BettingResponseDto updateBetting(BettingRequestDto requestDto){
        Betting betting = betting(requestDto.bettingId());

        if(!Objects.equals(betting.getUserId(), requestDto.userId())){          // 사용자 정보 다를 경우
            throw new BusinessException(BettingErrorCode.BETTING_UPDATE_ERROR);
        }else  if(betting.getGameId() != requestDto.gameId()){                  // 게임 정보 다를 경우
            throw new BusinessException(BettingErrorCode.BETTING_UPDATE_ERROR);
        }

        betting.updateBetting(
                requestDto.betAmount(),
                requestDto.odds(),
                requestDto.betType(),
                requestDto.betResult()
        );

        return BettingResponseDto.from(betting);
    }

    // 베팅 삭제
    public void deleteBetting(UUID bettingId, Long userId){
        // 베팅 존재 여부 확인
        Betting betting = betting(bettingId);

        betting.softDelete(userId);
    }
}
