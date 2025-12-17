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
public class NoLockBalanceUpdateStrategy extends AbstractBalanceUpdateStrategy {

    public NoLockBalanceUpdateStrategy(
            UserRepository userRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        super(userRepository, outboxRepository, objectMapper);
    }

    @Override
    protected User findAndUpdateUser(Long userId, UpdateBalanceCommand command) {
        // 일반 조회 (락 없음)
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.updateBalance(command.amount(), command.type());

        log.debug("[NO-LOCK] Balance updated without lock for userId: {}", userId);
        return user;
    }

    @Override
    protected String getStrategyName() {
        return "NO-LOCK";
    }

    @Override
    public String getType() {
        return "nolock";
    }
}