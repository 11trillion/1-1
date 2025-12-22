package com.oneonone.bettingservice.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.bettingservice.application.dto.BettingRequestDto;
import com.oneonone.bettingservice.application.dto.BettingResponseDto;
import com.oneonone.bettingservice.domain.entity.Betting;
import com.oneonone.bettingservice.domain.event.BettingEvent;
import com.oneonone.bettingservice.domain.event.GameCompletedEvent;
import com.oneonone.bettingservice.domain.repository.BettingRepository;
import com.oneonone.bettingservice.domain.vo.BetResult;
import com.oneonone.bettingservice.domain.vo.BettingErrorCode;
import com.oneonone.bettingservice.domain.vo.GameStatus;
import com.oneonone.bettingservice.infrastructure.client.UserServiceClient;
import com.oneonone.bettingservice.infrastructure.client.dto.BalanceResponse;
import com.oneonone.bettingservice.infrastructure.client.dto.GameResponse;
import com.oneonone.bettingservice.infrastructure.client.dto.GameServiceClient;
import com.oneonone.common.enums.GameResult;
import com.oneonone.common.enums.PointType;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BettingService {
    private final BettingRepository bettingRepository;
    private final KafkaTemplate<String, BettingEvent> kafkaPointReward;
    private final UserServiceClient userServiceClient;
    private final RedisTemplate<String, String> bettingHashRedisTemplate;
    private final GameServiceClient gameServiceClient;

    private HashOperations<String, String, String> hashOps() {
        return bettingHashRedisTemplate.opsForHash();
    }

    private String buildKey(UUID betId){
        return "bets:" + betId;
    }

    private String buildGameIndexKey(UUID gameId) {
        return "bets:game:" + gameId;
    }

    private String buildUserIndexKey(Long userId) {
        return "user:bets:" + userId;
    }
    
    // 경기 상태 검증
    private void validateGame(UUID gameId){
        ApiResponse<GameResponse> gameResultResponse =
                gameServiceClient.getGameById(gameId);

        GameResponse game = gameResultResponse.data();
        GameStatus status = game.status();

        if (!status.isScheduled()) {            // SCHEDULED 일 때만 베팅 생성, 수정 허용
            throw new BusinessException(BettingErrorCode.BETTING_CLOSED);
        }

    }

    private Map<String, String> getBettingMap(String key){
        try{
            Map<String, String> map = hashOps().entries(key);
            if (map.isEmpty()) {
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
        // 종료된 경기에는 베팅을 하지 못하도록 차단
        validateGame(requestDto.gameId());

        // 잔액 조회
        ApiResponse<BalanceResponse> balanceResponse = userServiceClient.getBalance(
                userId, "betting-service", "true" );
        Long currentBalance = balanceResponse.data().pointBalance();
        BigDecimal betAmount = requestDto.betAmount();
        BigDecimal currentBalanceDecimal = BigDecimal.valueOf(currentBalance);
        // Redis에서
        BigDecimal pendingBetAmount = getPendingBetAmount(userId);
        // 남은 포인트 계산
        BigDecimal remainingDecimal =
                currentBalanceDecimal
                        .subtract(pendingBetAmount)
                        .subtract(betAmount);
        long remainingPoint = remainingDecimal.longValue(); // 소수점 없다는 전제

        // 잔액 검증
        if (remainingDecimal.compareTo(BigDecimal.ZERO)  < 0) {
            log.warn("[Betting] 포인트 부족 - userId={}, current={}, required={}",
                    userId, currentBalance, betAmount);
            throw new BusinessException(BettingErrorCode.INSUFFICIENT_BALANCE);
        }

        // Redis에 베팅 내역 저장
        log.info("Redis 저장");
        UUID betId = UUID.randomUUID();
        UUID gameId = requestDto.gameId();
        String key = buildKey(betId);

        Map<String,String> values = new HashMap<>();
        values.put("gameId", gameId.toString());
        values.put("userId", userId.toString());
        values.put("betId", betId.toString());
        values.put("betAmount", requestDto.betAmount().toPlainString());
        values.put("odds", requestDto.odds().toPlainString());
        values.put("betType", requestDto.betType().name());
        values.put("betResult", BetResult.PENDING.name());
        values.put("remainingPoint", Long.toString(remainingPoint));

        hashOps().putAll(key, values);

        // gameId별 betId 목록 인덱스
        String indexKey = buildGameIndexKey(gameId);        // "game:bets:" + gameId
        bettingHashRedisTemplate.opsForSet().add(indexKey, betId.toString());

        // userId별 BetId 인덱스
        String userIndexKey = buildUserIndexKey(userId);     // "user:bets:" + userId
        bettingHashRedisTemplate.opsForSet().add(userIndexKey, betId.toString());

        // 저장
        return BettingResponseDto.fromHash(values);
    }

    // 베팅 수정 - Redis에 데이터가 있다는 전제
    // 경기가 종료되면 Redis를 삭제 처리한다. 정산이 끝난 베팅은 수정 하지 않는게 맞음
    @Transactional
    public BettingResponseDto updateBetting(UUID betId, Long userId, BettingRequestDto requestDto){
        String key = buildKey(betId);
        Map<String, String> map = getBettingMap(key);

        // 사용자 검증
        Long ownerId = Long.valueOf(map.get("userId"));
        if (!Objects.equals(ownerId, userId)) {
            throw new BusinessException(BettingErrorCode.BETTING_UPDATE_ERROR);
        }

        // 경기 상태 검증 (경기 상태가 스케줄일때만 변경 가능)
        validateGame(requestDto.gameId());

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
        // Redis에서 베팅 데이터 읽기
        String betKey = buildKey(bettingId);
        Map<String, String> map = getBettingMap(betKey);

        // 소유자 검증
        Long ownerId = Long.valueOf(map.get("userId"));
        if (!Objects.equals(ownerId, userId)) {
            log.warn("[Betting] delete - owner mismatch. bettingId={}, ownerId={}, requestUser={}",
                    bettingId, ownerId, userId);
            throw new BusinessException(BettingErrorCode.BETTING_UPDATE_ERROR);
        }

        // gameId 가져와서 인덱스 정리
        UUID gameId = UUID.fromString(map.get("gameId"));

        // 메인 hash 삭제
        bettingHashRedisTemplate.delete(betKey);

        // gameId 인덱스에서 betId제거
        String indexKey = buildGameIndexKey(gameId);
        bettingHashRedisTemplate.opsForSet().remove(indexKey, bettingId.toString());
        log.info("[Betting] delete success. bettingId={}, userId={}, gameId={}",
                bettingId, userId, gameId);


        // 베팅 존재 여부 확인 -DB에서 작업할 때
//        Betting betting = betting(bettingId);
//
//        betting.softDelete(userId);
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

        // 유저별 최종 포인트 증감액 계산
        Map<Long, Long> userDeltaMap = new HashMap<>();

        for(Betting betting : bets){
            String sagaId = UUID.randomUUID().toString();
            String eventId = UUID.randomUUID().toString();
            Long userId = betting.getUserId();

            long amount;
            PointType pointType;

            if(betting.isWin()){
                // 이긴 경우 => + 보상금액
                amount = betting.calculateReward();
                pointType = PointType.CREDIT;
            }else {
                // 진 경우 => -베팅금액
                amount = betting.getBetAmount().longValue();
                pointType = PointType.DEBIT;
            }

            BettingEvent event = new BettingEvent(
                    sagaId,
                    eventId,
                    userId,
                    amount,
                    pointType,
                    betting.getId()
            );

            kafkaPointReward.send(
                    "betting-reward",
                    userId.toString(),          // key: userId
                    event
            ).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("sent settlement for user {} amount={}", userId, amount);
                } else {
                    log.error("failed to send settlement for user {}", userId, ex);
                }
            });
        }

        log.info("[GAME-RESULT] Game result processed - gameId={}, bets={}",
                requestDto.gameId(), bets.size());

        // Redis 캐시 삭제
        String indexKey = "bets:game:" + gameId;
        Set<String> betIds = bettingHashRedisTemplate.opsForSet().members(indexKey);
        if (betIds != null) {
            for (String betIdStr : betIds) {
                UUID betId = UUID.fromString(betIdStr);
                String betKey = buildKey(betId);            // "bets:{betId}"
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
            String betKey = buildKey(betId);            // "bets:{betId}"

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

    // Redis 베팅 내역 합계 구하기
    private BigDecimal getPendingBetAmount(Long userId) {
        String userIndexKey = buildUserIndexKey(userId);
        Set<String> betIds = bettingHashRedisTemplate.opsForSet().members(userIndexKey);
        if (betIds == null || betIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (String betIdStr : betIds) {
            String betKey = buildKey(UUID.fromString(betIdStr));   // "bets:{betId}"
            Map<String, String> map = hashOps().entries(betKey);
            if (map.isEmpty()) {
                continue;
            }
            String amountStr = map.get("betAmount");
            if (amountStr != null) {
                sum = sum.add(new BigDecimal(amountStr));
            }
        }
        return sum;
    }
}
