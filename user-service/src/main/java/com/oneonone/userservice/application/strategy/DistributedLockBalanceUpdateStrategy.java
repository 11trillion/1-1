package com.oneonone.userservice.application.strategy;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.application.command.UpdateBalanceCommand;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.presentation.dto.response.BalanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DistributedLockBalanceUpdateStrategy extends AbstractBalanceUpdateStrategy {

    private final RedissonClient redissonClient;

    private static final long WAIT_TIME = 10L; // 락 획득 대기 시간 (초)
    private static final long LEASE_TIME = 3L; // 락 자동 해제 시간 (초)

    public DistributedLockBalanceUpdateStrategy(
            UserRepository userRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            RedissonClient redissonClient
    ) {
        super(userRepository, outboxRepository, objectMapper);
        this.redissonClient = redissonClient;
    }

    @Override
    public BalanceResponse updateBalance(Long userId, UpdateBalanceCommand command) {
        String lockKey = "balance:lock:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도
            boolean acquired = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("[DISTRIBUTED-LOCK] Failed to acquire lock for userId: {} after {}s",
                        userId, WAIT_TIME);
                throw new BusinessException(UserErrorCode.LOCK_ACQUISITION_FAILED);
            }

            log.debug("[DISTRIBUTED-LOCK] Lock acquired for userId: {}", userId);

            // 부모 클래스의 updateBalance 실행
            return super.updateBalance(userId, command);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[DISTRIBUTED-LOCK] Lock acquisition interrupted for userId: {}", userId, e);
            throw new BusinessException(UserErrorCode.LOCK_INTERRUPTED);
        } finally {
            // 락 해제 (현재 스레드가 락을 보유하고 있는 경우에만)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[DISTRIBUTED-LOCK] Lock released for userId: {}", userId);
            }
        }
    }

    @Override
    protected User findAndUpdateUser(Long userId, UpdateBalanceCommand command) {
        // 분산 락으로 이미 동시성 제어가 되었으므로 일반 조회 사용
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.updateBalance(command.amount(), command.type());

        log.debug("[DISTRIBUTED-LOCK] Balance updated for userId: {}", userId);
        return user;
    }

    @Override
    protected String getStrategyName() {
        return "DISTRIBUTED-LOCK";
    }

    @Override
    public String getType() {
        return "distributedlock";
    }
}
