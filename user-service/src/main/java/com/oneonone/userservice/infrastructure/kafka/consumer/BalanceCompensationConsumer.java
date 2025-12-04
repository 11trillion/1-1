package com.oneonone.userservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceCompensationEvent;
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
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "balance-compensate-event",
            groupId = "user-service",
            containerFactory = "balanceEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(String payloadJson) {
        BalanceCompensationEvent event = null;
        try {
            event = objectMapper.readValue(
                    payloadJson, BalanceCompensationEvent.class
            );
            log.info("[KAFKA-CONSUME] [Balance Compensation] Received eventId={}, userId={}, amount={}", event.eventId(), event.userId(), event.amount());
            if (outboxRepository.existsByEventId(UUID.fromString(event.eventId()))) {
                log.warn("[KAFKA-CONSUME] [Balance Compensation] Duplicate event - eventId={}", event.eventId());
                return;
            }
            User user = userRepository.findByUserIdAndDeletedAtIsNull(event.userId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
            user.compensateBalance(event);
            log.info("[KAFKA-CONSUME] [Balance Compensation] Rollback balance for userId={}", event.userId());
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.fromString(event.eventId()),
                    event.userId(),
                    payloadJson
            );
            outboxRepository.save(outboxEvent);
            log.info("[KAFKA-CONSUME] [Balance Compensation] Outbox saved for eventId={}", event.eventId());
        } catch (JsonProcessingException e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Json parse error. payload={}", payloadJson, e);
        } catch (BusinessException e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Business error. eventId={}", event != null ? event.eventId() : "unknown");
        } catch (Exception e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Error processing payload={}", payloadJson, e);
            throw new RuntimeException(e);
        }
    }
}
