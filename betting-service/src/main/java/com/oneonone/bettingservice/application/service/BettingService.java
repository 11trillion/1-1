package com.oneonone.bettingservice.application.service;

import com.oneonone.bettingservice.presentation.dto.BettingRequestDto;
import com.oneonone.bettingservice.presentation.dto.BettingResponseDto;
import com.oneonone.bettingservice.domain.Betting;
import com.oneonone.bettingservice.domain.BettingErrorCode;
import com.oneonone.bettingservice.domain.BettingRepository;
import com.oneonone.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BettingService {
    private final BettingRepository bettingRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

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
    @Transactional
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
    @Transactional
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

        // Kafka
//        String topic = "point";
//        String key = "test";
//        String message = "경기 결과에 따른 포인트 업데이트";
//        kafkaTemplate.send(topic, key, message);

        return BettingResponseDto.from(betting);
    }

    // 베팅 삭제
    @Transactional
    public void deleteBetting(UUID bettingId, Long userId){
        // 베팅 존재 여부 확인
        Betting betting = betting(bettingId);

        betting.softDelete(userId);
    }

    // todo kafka 테스트 - 추후 삭제 예정
    public String kafkaTest(){
        // Kafka
        System.out.println("kafka 테스트 시작");
        String topic = "point";
        String key = "test";
        String message = "경기 결과에 따른 포인트 업데이트";
        kafkaTemplate.send(topic, key, message);

        return "완료";
    }

    // todo kafka 테스트 - 추후 삭제 예정
    @KafkaListener(groupId = "betting", topics = "point")
    public void cumsumerTest(String message){
        log.info("Kafka 테스트: " + message);
    }

}
