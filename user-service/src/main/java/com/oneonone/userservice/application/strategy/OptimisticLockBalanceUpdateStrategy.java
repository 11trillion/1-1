package com.oneonone.userservice.application.strategy;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.application.command.UpdateBalanceCommand;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OptimisticLockBalanceUpdateStrategy extends AbstractBalanceUpdateStrategy {

    public OptimisticLockBalanceUpdateStrategy(
            UserRepository userRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        super(userRepository, outboxRepository, objectMapper);
    }

    @Override
    protected User findAndUpdateUser(Long userId, UpdateBalanceCommand command) {
        // 낙관적 락은 Entity의 @Version 필드로 처리됨
        // Repository에서 명시적으로 OPTIMISTIC Lock을 걸 수도 있음
        User user = userRepository.findByIdWithOptimisticLock(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.updateBalance(command.amount(), command.type());

        // 커밋 시점에 version 체크가 일어남
        // 충돌 시 OptimisticLockException 발생
        log.debug("[OPTIMISTIC-LOCK] Balance updated with optimistic lock for userId: {}", userId);
        return user;
    }

    @Override
    protected String getStrategyName() {
        return "OPTIMISTIC-LOCK";
    }

    @Override
    public String getType() {
        return "optimisticlock";
    }
}