package com.oneonone.userservice.infrastructure.kafka.consumer;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.domain.entity.CompensationEvent;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.CompensationEventRepository;
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
    private final CompensationEventRepository compensationEventRepository;
    private final CompensationResultProducer compensationResultProducer;

    @KafkaListener(
            topics = "${spring.kafka.topics.balance-compensation-event}",
            groupId = "user-service",
            containerFactory = "balanceCompensationEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(BalanceCompensationEvent event) {
        try {
            log.info("[KAFKA-CONSUME] [Balance Compensation] Received - sagaId={}, eventId={}, userId={}, amount={}",
                    event.sagaId(), event.eventId(), event.userId(), event.amount());


            // 중복 이벤트 체크
            UUID originalEventId = UUID.fromString(event.eventId());
            if (compensationEventRepository.existsByOriginalEventId(originalEventId)) {
                log.warn("[KAFKA-CONSUME] [Balance Compensation] Duplicate compensation event - sagaId={}, eventId={}",
                        event.sagaId(), event.eventId());

                // 이미 처리됨 → 성공 응답
                compensationResultProducer.sendCompensationResult(
                        new CompensationResultEvent(event.sagaId(), event.eventId(), true, null)
                );
                return;
            }
            // 사용자 조회 및 보상 처리
            User user = userRepository.findByUserIdAndDeletedAtIsNull(event.userId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

            log.info("[KAFKA-CONSUME] [Balance Compensation] Before rollback - userId={}, balance={}",
                    user.getUserId(), user.getPointBalance());

            user.compensateBalance(event);

            log.info("[KAFKA-CONSUME] [Balance Compensation] After rollback - userId={}, balance={}",
                    user.getUserId(), user.getPointBalance());

            CompensationEvent compensationRecord =
                    CompensationEvent.success(
                            originalEventId,
                            event.userId(),
                            event.amount(),
                            event.type()
                    );
            compensationEventRepository.save(compensationRecord);

            compensationResultProducer.sendCompensationResult(
                    new CompensationResultEvent(event.sagaId(), event.eventId(), true, null)
            );

            log.info("[KAFKA-CONSUME] [Balance Compensation] Completed successfully - sagaId={}, eventId={}",
                    event.sagaId(), event.eventId());
        } catch (BusinessException e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Business error - sagaId={}, eventId={}",
                    event != null ? event.sagaId() : "unknown",
                    event != null ? event.eventId() : "unknown", e);

            // 비즈니스 로직 실패 시 실패 신호 전송
            if (event != null) {
                try {
                    CompensationEvent compensationRecord =
                            CompensationEvent.failure(
                                    UUID.fromString(event.eventId()),
                                    event.userId(),
                                    event.amount(),
                                    event.type(),
                                    e.getMessage()
                            );
                    compensationEventRepository.save(compensationRecord);
                } catch (Exception saveError) {
                    log.error("[KAFKA-CONSUME] [Balance Compensation] Failed to save compensation record", saveError);
                }

                compensationResultProducer.sendCompensationResult(
                        new CompensationResultEvent(event.sagaId(), event.eventId(), false, e.getMessage())
                );
            }
            throw e;
        } catch (Exception e) {
            log.error("[KAFKA-CONSUME] [Balance Compensation] Unexpected error - sagaId={}, eventId={}",
                    event != null ? event.sagaId() : "unknown",
                    event != null ? event.eventId() : "unknown", e);

            if (event != null) {
                compensationResultProducer.sendCompensationResult(
                        new CompensationResultEvent(event.sagaId(), event.eventId(), false, "Unexpected error: " + e.getMessage())
                );
            }

            throw new RuntimeException("Failed to process balance compensation event", e);
        }
    }
}
