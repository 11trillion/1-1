package com.oneonone.bettingservice.domain.service;

import com.oneonone.bettingservice.application.dto.BettingRequestDto;
import com.oneonone.bettingservice.application.dto.BettingResponseDto;
import com.oneonone.bettingservice.domain.entity.Betting;
import com.oneonone.bettingservice.domain.event.BettingEvent;
import com.oneonone.bettingservice.domain.event.GameCompletedEvent;
import com.oneonone.bettingservice.domain.repository.BettingRepository;
import com.oneonone.bettingservice.domain.vo.BetResult;
import com.oneonone.bettingservice.domain.vo.BettingErrorCode;
import com.oneonone.bettingservice.infrastructure.client.UserServiceClient;
import com.oneonone.bettingservice.infrastructure.client.dto.BalanceResponse;
import com.oneonone.common.enums.GameResult;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BettingService {
    private final BettingRepository bettingRepository;
    private final KafkaTemplate<String, BettingEvent> kafkaPointReward;
    private final UserServiceClient userServiceClient;
    private final RedisTemplate<String, String> bettingHashRedisTemplate;

    private HashOperations<String, String, String> hashOps() {
        return bettingHashRedisTemplate.opsForHash();
    }

    private String buildKey(UUID gameId, UUID betId){
        return "bets:" + gameId + ":" + betId;
    }

    private Map<String, String> getBettingMap(String key){
        try{
            Map<String, String> map = hashOps().entries(key);
            if (map == null || map.isEmpty()) {
                log.error("[Betting] Redis betting not found - key={}", key);
                throw new BusinessException(BettingErrorCode.BETTING_NOT_FOUND);
            }
            return map;
        }catch (Exception e){
            log.error("[Betting] Redis betting load failed - key={}", key, e);
            throw new BusinessException(BettingErrorCode.BETTING_REDIS_ERROR);
        }
    }

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
        ApiResponse<BalanceResponse> balanceResponse = userServiceClient.getBalance(
                userId, "betting-service", "true" );
        Long currentBalance = balanceResponse.data().pointBalance();
        BigDecimal betAmount = requestDto.betAmount();
        BigDecimal currentBalanceDecimal = BigDecimal.valueOf(currentBalance);

        // 잔액 검증
        if (currentBalanceDecimal.compareTo(betAmount) < 0) {
            log.warn("[Betting] 포인트 부족 - userId={}, current={}, required={}",
                    userId, currentBalance, betAmount);
            throw new BusinessException(BettingErrorCode.INSUFFICIENT_BALANCE);
        }

        // Redis에 베팅 내역 저장
        log.info("Redis 저장");
        // Redis에 저장
        UUID betId = UUID.randomUUID();
        UUID gameId = requestDto.gameId();
        String key = buildKey(betId, gameId);

        hashOps().put(key, "gameId", gameId.toString());
        hashOps().put(key, "betId", betId.toString());
        hashOps().put(key, "userId", userId.toString());
        hashOps().put(key, "betAmount", requestDto.betAmount().toPlainString());
        hashOps().put(key, "odds", requestDto.odds().toPlainString());
        hashOps().put(key, "betType", requestDto.betType().name());

        // 2) gameId별 betId 목록 인덱스
        String indexKey = "bets:game:" + gameId;
        bettingHashRedisTemplate.opsForSet().add(indexKey, betId.toString());

        Map<String, String> map = getBettingMap(key);

        // todo 베팅 저장되면서 포인트 차감 처리

        // 저장
        return BettingResponseDto.fromHash(map);
    }

    // 베팅 수정 - Redis에 데이터가 있다는 전제
    // 경기가 종료되면 Redis를 삭제 처리한다. 정산이 끝난 베팅은 수정 하지 않는게 맞음
    @Transactional
    public BettingResponseDto updateBetting(UUID betId, Long userId, BettingRequestDto requestDto){
        UUID gameId = requestDto.gameId();
        String key = buildKey(betId, gameId);
        Map<String, String> map = getBettingMap(key);

        // 사용자 검증
        Long ownerId = Long.valueOf(map.get("userId"));
        if (!Objects.equals(ownerId, userId)) {
            throw new BusinessException(BettingErrorCode.BETTING_UPDATE_ERROR);
        }

        // 경기 상태 검증 (진행중 아니면 수정 불가) -> todo 경기중?, 미정? 경기중일떄는 못 바꾸도록 해야되는거 아닌가?
        BetResult betResult = BetResult.valueOf(map.getOrDefault("betResult", "PENDING"));
        if (betResult != BetResult.PENDING) {
            throw new BusinessException(BettingErrorCode.BETTING_CLOSED);
        }

        // Redis 데이터 수정
        if (requestDto.betAmount() != null) {
            hashOps().put(key, "betAmount", requestDto.betAmount().toPlainString());
        }
        if (requestDto.odds() != null) {
            hashOps().put(key, "odds", requestDto.odds().toPlainString());
        }
        if (requestDto.betType() != null) {
            hashOps().put(key, "betType", requestDto.betType().name());     // HOME_WIN, AWAY_WIN, DRAW
        }

        // 수정된 값을 다시 읽음
        Map<String, String> updated = hashOps().entries(key);

        return BettingResponseDto.fromHash(updated);
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
        UUID gameId = requestDto.gameId();
        GameResult gameResult = requestDto.gameResult();

        // 1. Redis 데이터로 DB 데이터 생성
        List<Betting> bets = loadBetsFromRedis(gameId);
        if (bets.isEmpty()) {
            throw new BusinessException(BettingErrorCode.BETTING_NOT_FOUND);
        }

        // 2. DB에 경기결과 반영 (Redis에는 반영 할 필요 없음)
        for(Betting betting : bets){
            BetResult betResult =
                    betting.getBetType().equals(gameResult)
                    ? BetResult.WIN : BetResult.LOSE;
            betting.updateResult(betResult);
        }

        // 3. JPA 저장
        bettingRepository.saveAll(bets);

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

        // Redis 캐시 삭제
        String indexKey = "bets:game:" + gameId;
        Set<String> betIds = bettingHashRedisTemplate.opsForSet().members(indexKey);
        if (betIds != null) {
            for (String betIdStr : betIds) {
                UUID betId = UUID.fromString(betIdStr);
                String betKey = buildKey(betId, gameId);    // "bets:{gameId}:{betId}"
                bettingHashRedisTemplate.delete(betKey);    // 상세 해시 삭제
            }
        }
        bettingHashRedisTemplate.delete(indexKey);          // 인덱스 Set 삭제
    }

    // Redis에서 gameId로 베팅 목록 읽어오기
    private List<Betting> loadBetsFromRedis(UUID gameId) {
        String indexKey = "bets:game:" + gameId;
        Set<String> betIds = bettingHashRedisTemplate.opsForSet().members(indexKey);
        if (betIds == null || betIds.isEmpty()) {
            return List.of();
        }

        HashOperations<String, String, String> hashOps = bettingHashRedisTemplate.opsForHash();
        List<Betting> result = new ArrayList<>();

        for (String betIdStr : betIds) {
            UUID betId = UUID.fromString(betIdStr);
            String betKey = buildKey(betId, gameId);  // "bets:{gameId}:{betId}"

            Map<String, String> map = hashOps.entries(betKey);
            if (map.isEmpty()) {
                continue;
            }

            // Redis 해시 → Betting 엔티티(또는 정산 전용 VO) 변환
            Betting betting = Betting.fromRedis(
                    betId,
                    Long.valueOf(map.get("userId")),
                    gameId,
                    new BigDecimal(map.get("betAmount")),
                    new BigDecimal(map.get("odds")),
                    GameResult.valueOf(map.get("betType"))
            );
            result.add(betting);
        }
        return result;
    }
}
