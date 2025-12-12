
package com.oneonone.userservice.infrastructure.kafka.consumer;

import com.oneonone.common.enums.PointType;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceEvent;
import com.oneonone.userservice.infrastructure.kafka.event.BettingEvent;
import com.oneonone.userservice.infrastructure.kafka.producer.UserKafkaProducer;
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
    private final UserKafkaProducer userKafkaProducer;

    @KafkaListener(
            topics = "${spring.kafka.topics.betting-reward}",
            groupId = "user-service",
            containerFactory = "bettingEventConcurrentKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(BettingEvent event) {
        // Betting Service에서 생성한 sagaId
        String sagaId = event.sagaId();

        log.info("[KAFKA-CONSUME] [Betting] Received eventId={}, userId={}: ", event.eventId(), event.userId());
        if (processedBettingEventRepository.existsByBetId(UUID.fromString(event.betId()))) {
            log.warn("[KAFKA-CONSUME] [Betting] Already processed eventId={}", event.eventId());
            return;
        }
        processedBettingEventRepository.save(ProcessedBettingEvent.of(UUID.fromString(event.eventId()), event.userId(), UUID.fromString(event.betId()), event.amount()));
        try {
            User user = userRepository.findByUserIdAndDeletedAtIsNull(event.userId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
            log.info("[KAFKA-CONSUME] [Betting] PointBalance Before Update for userId={}, pointBalance={}", user.getUserId(), user.getPointBalance());
            user.updateBalance(event.amount(), PointType.CREDIT);
            log.info("[KAFKA-CONSUME] [Betting] PointBalance After Update userId={}, pointBalance={}", user.getUserId(), user.getPointBalance());
            BalanceEvent balanceEvent = new BalanceEvent(
                    event.sagaId(), // Betting Service에서 생성한 sagaId
                    event.eventId(),
                    event.userId(),
                    event.amount(),
                    PointType.CREDIT,
                    event.betId()
            );
            userKafkaProducer.send(balanceEvent);
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
