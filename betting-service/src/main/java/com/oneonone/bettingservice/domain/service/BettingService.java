package com.oneonone.bettingservice.domain.service;

import com.oneonone.bettingservice.domain.vo.BetResult;
import com.oneonone.bettingservice.domain.entity.Betting;
import com.oneonone.bettingservice.domain.vo.BettingErrorCode;
import com.oneonone.bettingservice.domain.repository.BettingRepository;
import com.oneonone.bettingservice.domain.event.BettingEvent;
import com.oneonone.bettingservice.domain.event.GameCompletedEvent;
import com.oneonone.bettingservice.application.dto.BettingRequestDto;
import com.oneonone.bettingservice.application.dto.BettingResponseDto;
import com.oneonone.bettingservice.infrastructure.client.UserServiceClient;
import com.oneonone.bettingservice.infrastructure.client.dto.BalanceResponse;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.response.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BettingService {
    private final BettingRepository bettingRepository;
    private final KafkaTemplate<String, BettingEvent> kafkaPointReward;
    private final UserServiceClient userServiceClient;

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
    public BettingResponseDto createBetting(Long userId ,BettingRequestDto requestDto){
        // todo 종료된 경기에는 베팅을 하지 못하도록 차단 -> 게임한테 경기정보를 받아와야할 것 같음

        // todo 회원에서 현재 포인트 받아와서 정보 가지고 있기
        UUID sagaId = UUID.randomUUID();

        // 잔액 조회
        ApiResponse<BalanceResponse> balanceResponse = userServiceClient.getBalance(userId);
        Long currentBalance = balanceResponse.data().pointBalance();
        BigDecimal betAmount = requestDto.betAmount();
        BigDecimal currentBalanceDecimal = BigDecimal.valueOf(currentBalance);

        // 잔액 검증
        if (currentBalanceDecimal.compareTo(betAmount) < 0) {
            log.warn("[Betting] 포인트 부족 - userId={}, current={}, required={}",
                    userId, currentBalance, betAmount);
            throw new BusinessException(BettingErrorCode.INSUFFICIENT_BALANCE);
        }

        // 생성
        Betting betting = Betting.createBetting(
                userId,
                requestDto.gameId(),
                requestDto.betAmount(),
                requestDto.odds(),
                requestDto.betType()
        );

        // todo Redis에 베팅 내역 저장
        // todo 베팅 저장되면서 포인트 차감 처리

        // 저장
        bettingRepository.save(betting);

        return  BettingResponseDto.from(betting);
    }

    // 베팅 수정
    @Transactional
    public BettingResponseDto updateBetting(UUID betId, Long userId, BettingRequestDto requestDto){
        Betting betting = betting(betId);

        // todo 조건에 대해서 좀 더 고민하기
        if(!Objects.equals(betting.getUserId(), userId)){          // 사용자 정보 다를 경우
            throw new BusinessException(BettingErrorCode.BETTING_UPDATE_ERROR);
        }

        betting.updateBetting(
                requestDto.betAmount(),
                requestDto.odds(),
                requestDto.betType()            // HOME_WIN, AWAY_WIN, DRAW
        );

        return BettingResponseDto.from(betting);
    }

    // 베팅 삭제
    @Transactional
    public void deleteBetting(UUID bettingId, Long userId){
        // 베팅 존재 여부 확인
        Betting betting = betting(bettingId);

        betting.softDelete(userId);
    }

    // Kafka로 게임 결과를 받고 베팅금액 * 배당률을 계산 후 회원 서비스에 보내서 포인트를 업데이트 한다.
    @Transactional
    public void updateGameResult(GameCompletedEvent requestDto){
        log.info("updateGameResult");
        // 게임 아이디를 받아서 경기결과를 업데이트 해준다.
        List<Betting> bets = bettingRepository
                .findAllByGameId(requestDto.gameId())
                .orElseThrow(()-> new BusinessException(BettingErrorCode.BETTING_NOT_FOUND));

        for (Betting tempBetting : bets) {
            if (tempBetting.getBetType().equals(requestDto.gameResult())) {
                tempBetting.updateResult(BetResult.WIN);               // 승부예측이 맞을 경우
            } else {
                tempBetting.updateResult(BetResult.LOSE);              // 승부예측이 틀린 경우
            }
        }

        // 정산 계산 후 회원 모듈에게 포인트를 수정하도록 요청한다.
        List<BettingEvent> rewards = bets.stream()
                .filter(Betting::isWin)
                .map(b -> {
                    String sagaId = UUID.randomUUID().toString();
                    String eventId = UUID.randomUUID().toString();

                    log.info("[GAME-RESULT] Creating reward - sagaId={}, betId={}, userId={}, amount={}",
                            sagaId, b.getId(), b.getUserId(), b.calculateReward());

                    return new BettingEvent(
                            sagaId,
                            eventId,
                            b.getUserId(),
                            b.calculateReward(),
                            b.getId().toString()
                    );
                })
                .toList();

        // 포인트 서비스 이벤트 발행
        rewards.forEach(event ->
                kafkaPointReward.send(
                        "betting-reward",
                        event.userId().toString(),  // key: userId
                        event                       // value : Long balance
                ).whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("sent pointReward for user {}", event.userId());
                    } else {
                        log.error("failed to send pointReward for user {}", event.userId(), ex);
                    }
                })
        );

        log.info("[GAME-RESULT] Game result processed - gameId={}, totalRewards={}",
                requestDto.gameId(), rewards.size());
    }
}
