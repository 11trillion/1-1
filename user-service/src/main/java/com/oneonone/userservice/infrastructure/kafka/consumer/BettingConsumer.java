
package com.oneonone.userservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.enums.PointType;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceEvent;
import com.oneonone.userservice.infrastructure.kafka.event.BettingEvent;
import com.oneonone.userservice.infrastructure.persistence.entity.ProcessedBettingEvent;
import com.oneonone.userservice.infrastructure.persistence.repository.ProcessedBettingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BettingConsumer {

    private final UserRepository userRepository;
    private final ProcessedBettingEventRepository processedBettingEventRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topics.betting-reward}",
            groupId = "user-service",
            containerFactory = "bettingEventConcurrentKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(BettingEvent event) throws JsonProcessingException {
        log.info("[KAFKA-CONSUME] [Betting] Received eventId={}, userId={}: ", event.eventId(), event.userId());
        if (processedBettingEventRepository.existsByEventId(UUID.fromString(event.eventId()))) {
            log.warn("[KAFKA-CONSUME] [Betting] Already processed eventId={}", event.eventId());
            return;
        }
        try {
            User user = userRepository.findByUserIdAndDeletedAtIsNull(event.userId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
            log.info("[KAFKA-CONSUME] [Betting] PointBalance Before Update for userId={}, pointBalance={}", user.getUserId(), user.getPointBalance());
            user.updateBalance(event.amount(), PointType.CREDIT);
            log.info("[KAFKA-CONSUME] [Betting] PointBalance After Update userId={}, pointBalance={}", user.getUserId(), user.getPointBalance());
            String sagaId = UUID.randomUUID().toString(); // TODO: 임시 sagaId
            BalanceEvent balanceEvent = new BalanceEvent(
                    sagaId, // TODO: Betting에서 받아오도록 수정
                    event.eventId(),
                    event.userId(),
                    event.amount(),
                    PointType.CREDIT,
                    event.betId()
            );
            String payload = objectMapper.writeValueAsString(balanceEvent);
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.fromString(sagaId), // TODO: DLT TEST -> null 처리
                    UUID.fromString(event.eventId()),
                    event.userId(),
                    payload
            );
            outboxRepository.save(outboxEvent);
            log.info("[KAKFA-CONUSME] [Betting] Outbox saved - outboxId={}, balanceEventId={}", outboxEvent.getOutboxId(), balanceEvent.eventId());
            processedBettingEventRepository.save(ProcessedBettingEvent.of(UUID.fromString(event.eventId()), event.userId(), UUID.fromString(event.betId()), event.amount()));
            log.info("[KAFKA-CONSUME] [Betting] Successfully processed eventId={}", event.eventId());
        } catch (BusinessException e) {
            log.error("[KAFKA-CONSUME] [Betting] Business error - eventId={}, errorCode={}", event.eventId(), e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("[KAFKA-CONSUME] [Betting] Retryable error - eventId={}", event.eventId(), e);
            throw e;
        }
    }
}
