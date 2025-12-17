package com.oneonone.userservice.application.strategy;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.application.command.UpdateBalanceCommand;
import com.oneonone.userservice.application.event.BalanceEventPayload;
import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.presentation.dto.response.BalanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBalanceUpdateStrategy implements BalanceUpdateStrategy {

    protected final UserRepository userRepository;
    protected final OutboxRepository outboxRepository;
    protected final ObjectMapper objectMapper;

    @Override
    @Transactional
    public BalanceResponse updateBalance(Long userId, UpdateBalanceCommand command) {
        log.info("[{}] Starting balance update for userId: {}, amount: {}",
                getStrategyName(), userId, command.amount());

        // 1. 멱등성 체크 - 이미 처리된 eventId인지 확인
        if (isDuplicateEvent(command.eventId())) {
            log.info("[{}] Duplicate eventId detected: {}, returning existing result",
                    getStrategyName(), command.eventId());
            return getExistingBalance(userId);
        }

        // 2. User 조회 + Balance 업데이트 (전략별로 다름)
        User user = findAndUpdateUser(userId, command);

        // 3. Outbox 이벤트 저장 (공통)
        saveOutboxEvent(user, command);

        log.info("[{}] Successfully updated balance for userId: {}, new balance: {}",
                getStrategyName(), userId, user.getPointBalance());

        return new BalanceResponse(userId, user.getPointBalance());
    }

    /**
     * 전략별로 구현해야 하는 메서드
     * 각 락 전략에 맞게 User를 조회하고 Balance를 업데이트
     */
    protected abstract User findAndUpdateUser(Long userId, UpdateBalanceCommand command);

    /**
     * 로깅용 전략 이름 반환
     */
    protected abstract String getStrategyName();

    /**
     * 중복 이벤트 체크
     */
    private boolean isDuplicateEvent(UUID eventId) {
        Optional<OutboxEvent> existingEvent = outboxRepository.findByEventId(eventId);
        return existingEvent.isPresent();
    }

    /**
     * 중복 이벤트인 경우 기존 잔액 반환
     */
    private BalanceResponse getExistingBalance(Long userId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return new BalanceResponse(userId, user.getPointBalance());
    }

    /**
     * Outbox 이벤트 생성 및 저장
     */
    private void saveOutboxEvent(User user, UpdateBalanceCommand command) {
        BalanceEventPayload event = new BalanceEventPayload(
                command.sagaId().toString(),
                command.eventId().toString(),
                user.getUserId(),
                command.amount(),
                command.type(),
                command.betId() != null ? command.betId().toString() : null
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
            log.debug("[{}] Created payload: {}", getStrategyName(), payload);
        } catch (JsonProcessingException e) {
            log.error("[{}] Failed to serialize payload", getStrategyName(), e);
            throw new BusinessException(UserErrorCode.OUTBOX_PAYLOAD_ERROR);
        }

        OutboxEvent outboxEvent = new OutboxEvent(
                command.sagaId(),
                command.eventId(),
                user.getUserId(),
                payload
        );
        outboxRepository.save(outboxEvent);
    }
}
