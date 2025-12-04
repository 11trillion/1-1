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

//    @KafkaListener(
//            topics = "balance-compensation-event",
//            groupId = "user-service",
//            containerFactory = "balanceEventKafkaListenerContainerFactory"
//    )
//    @Transactional
//    public void consume(BalanceCompensationEventPayload payload) {
//        log.info("[KAFKA-CONSUME] BalanceCompensationConsumer Raw payload received: {}", payload);
//
//        try {
//            if (outboxRepository.existsByEventId(UUID.fromString(payload.eventId()))) {
//                log.warn("[KAFKA-CONSUME] [Balance Compensation] Duplicate event - eventId={}", payload.eventId());
//                return;
//            }
//
//            // 사용자 조회
//            User user = userRepository.findByUserIdAndDeletedAtIsNull(payload.userId())
//                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
//            user.compensateBalance(payload);
//            log.info("[KAFKA-CONSUME] [Balance Compensation] Rollback balance for userId={}", payload.userId());
//            OutboxEvent outboxEvent = new OutboxEvent(
//                    UUID.fromString(payload.eventId()),
//                    payload.userId(),
//                    payload.toString()
//            );
//            outboxRepository.save(outboxEvent);
//            log.info("[KAFKA-CONSUME] [Balance Compensation] Outbox saved for eventId={}", payload.eventId());
//        } catch (BusinessException e) {
//            log.error("[KAFKA-CONSUME] Business error. eventId={}", payload.eventId(), e);
//        } catch (Exception e) {
//            log.error("[KAFKA-CONSUME] [Balance Compensation] Error processing payload={}", payload, e);
//            throw new RuntimeException(e);
//        }
//    }

    @KafkaListener(
            topics = "balance-compensation-event",
            groupId = "user-service"
    )
    @Transactional
    public void consume(String payloadJson) {
        try {
            BalanceCompensationEvent event = objectMapper.readValue(payloadJson, BalanceCompensationEvent.class);
            log.info("[KAFKA-CONSUME] [Balance Compensation] Received eventId={}, userId={}, amount={}",
                    event.eventId(), event.userId(), event.amount());

            // 중복 이벤트 체크
            if (outboxRepository.existsByEventId(UUID.fromString(event.eventId()))) {
                log.warn("[KAFKA-CONSUME] [Balance Compensation] Duplicate compensation event - eventId={}",
                        event.eventId());
                return;
            }

            // 사용자 조회 및 보상 처리
            User user = userRepository.findByUserIdAndDeletedAtIsNull(event.userId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
            user.compensateBalance(event);
            log.info("[KAFKA-CONSUME] [Balance Compensation] Rollback balance for userId={}", event.userId());
        } catch (JsonProcessingException e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Json serialize error.", e);
            throw new RuntimeException("Failed to serialize event", e);
        } catch (BusinessException e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Business error.", e);
            throw e;
        } catch (Exception e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Unexpected error processing.", e);
            throw new RuntimeException("Failed to process balance compensation event", e);
        }
    }
}