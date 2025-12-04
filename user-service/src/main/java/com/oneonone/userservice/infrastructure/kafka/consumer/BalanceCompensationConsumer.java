package com.oneonone.userservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.infrastructure.kafka.BalanceCompensationEventPayload;
import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceCompensationConsumer {
    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;

    @KafkaListener(
            topics = "balance-compensation-event",
            groupId = "user-service",
            containerFactory = "balanceEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(BalanceCompensationEventPayload payload) {
        log.info("[KAFKA-CONSUME] BalanceCompensationConsumer Raw payload received: {}", payload);

        try {
            if (outboxRepository.existsByEventId(UUID.fromString(payload.eventId()))) {
                log.warn("[KAFKA-CONSUME] [Balance Compensation] Duplicate event - eventId={}", payload.eventId());
                return;
            }

            // 사용자 조회
            User user = userRepository.findByUserIdAndDeletedAtIsNull(payload.userId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
            user.compensateBalance(payload);
            log.info("[KAFKA-CONSUME] [Balance Compensation] Rollback balance for userId={}", payload.userId());
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.fromString(payload.eventId()),
                    payload.userId(),
                    payload.toString()
            );
            outboxRepository.save(outboxEvent);
            log.info("[KAFKA-CONSUME] [Balance Compensation] Outbox saved for eventId={}", payload.eventId());
        } catch (BusinessException e) {
            log.error("[KAFKA-CONSUME] Business error. eventId={}", payload.eventId(), e);
        } catch (Exception e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Error processing payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }
}
