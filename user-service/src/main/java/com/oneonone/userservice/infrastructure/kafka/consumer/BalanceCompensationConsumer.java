package com.oneonone.userservice.infrastructure.kafka.consumer;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceCompensationEvent;
import com.oneonone.userservice.infrastructure.kafka.event.CompensationResultEvent;
import com.oneonone.userservice.infrastructure.kafka.producer.CompensationResultProducer;
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
    private final CompensationResultProducer compensationResultProducer;

    @KafkaListener(
            topics = "balance-compensation-event",
            groupId = "user-service",
            containerFactory = "balanceCompensationEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(BalanceCompensationEvent event) {
        try {
            log.info("[KAFKA-CONSUME] [Balance Compensation] Received eventId={}, userId={}, amount={}",
                    event.eventId(), event.userId(), event.amount());

            // 중복 이벤트 체크
            if (outboxRepository.existsByEventId(UUID.fromString(event.eventId()))) {
                log.warn("[KAFKA-CONSUME] [Balance Compensation] Duplicate compensation event - eventId={}",
                        event.eventId());

                compensationResultProducer.sendCompensationResult(
                        new CompensationResultEvent(event.eventId(), true, null)
                );
                return;
            }

            // 사용자 조회 및 보상 처리
            User user = userRepository.findByUserIdAndDeletedAtIsNull(event.userId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
            user.compensateBalance(event);
            log.info("[KAFKA-CONSUME] [Balance Compensation] Rollback balance for userId={}", event.userId());

            compensationResultProducer.sendCompensationResult(
                    new CompensationResultEvent(event.eventId(), true, null)
            );
        } catch (BusinessException e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Business error - eventId={}",
                    event != null ? event.eventId() : "unknown", e);

            // 비즈니스 로직 실패 시 실패 신호 전송
            if (event != null) {
                compensationResultProducer.sendCompensationResult(
                        new CompensationResultEvent(event.eventId(), false, e.getMessage())
                );
            }
            throw e;
        } catch (Exception e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Unexpected error - eventId={}",
                    event != null ? event.eventId() : "unknown", e);

            if (event != null) {
                compensationResultProducer.sendCompensationResult(
                        new CompensationResultEvent(event.eventId(), false, "Unexpected error: " + e.getMessage())
                );
            }

            throw new RuntimeException("Failed to process balance compensation event", e);
        }
    }
}