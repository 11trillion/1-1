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
public class PessimisticLockBalanceUpdateStrategy extends AbstractBalanceUpdateStrategy {

    public PessimisticLockBalanceUpdateStrategy(
            UserRepository userRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        super(userRepository, outboxRepository, objectMapper);
    }

    @Override
    protected User findAndUpdateUser(Long userId, UpdateBalanceCommand command) {
        // DB 레벨에서 SELECT ... FOR UPDATE 실행
        // 다른 트랜잭션이 해당 row를 수정하지 못하도록 락 획득
        User user = userRepository.findByIdWithPessimisticLock(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.updateBalance(command.amount(), command.type());

        log.debug("[PESSIMISTIC-LOCK] Balance updated with pessimistic lock for userId: {}", userId);
        return user;
    }

    @Override
    protected String getStrategyName() {
        return "PESSIMISTIC-LOCK";
    }

    @Override
    public String getType() {
        return "pessimisticlock";
    }
}