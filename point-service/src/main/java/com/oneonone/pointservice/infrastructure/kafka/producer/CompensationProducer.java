package com.oneonone.pointservice.infrastructure.kafka.producer;

import com.oneonone.pointservice.infrastructure.kafka.event.BalanceCompensationEvent;
import com.oneonone.pointservice.infrastructure.kafka.event.CompensationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationProducer {

    private static final String COMPENSATION_TOPIC = "balance-compensation-event";

    private final KafkaTemplate<String, BalanceCompensationEvent> kafkaTemplate;

    /**
     * 보상 이벤트를 동기 방식으로 발행합니다.
     * 발행 실패 시 예외가 발생하여 호출자가 처리할 수 있습니다.
     */
    public void sendCompensation(CompensationEvent event) {
        try {
            BalanceCompensationEvent payload = new BalanceCompensationEvent(
                    event.sagaId(),    // 동일한 sagaId
                    event.eventId(),
                    event.userId(),
                    event.amount(),
                    event.type(),
                    event.betId()
            );
            SendResult<String, BalanceCompensationEvent> result = kafkaTemplate
                    .send(COMPENSATION_TOPIC, payload.eventId(), payload)
                    .get(); // 동기 대기

            log.info("[COMPENSATION-SENT] Successfully published - eventId={}, topic={}, partition={}",
                    payload.eventId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition());

        } catch (Exception e) {
            log.error("[COMPENSATION-FAILED] Failed to publish compensation event - sagaId={}, eventId={}, error={}",
                    event.sagaId(),
                    event.eventId(),
                    e.getMessage());
            throw new CompensationPublishException("Failed to publish compensation event", e);
        }
    }

    /**
     * CompensationEvent -> BalanceCompensationEventPayload 변환
     */
    private BalanceCompensationEvent mapToPayload(CompensationEvent event) {
        return new BalanceCompensationEvent(
                event.sagaId(),
                event.eventId(),
                event.userId(),
                event.amount(),
                event.type(),
                event.betId()
        );
    }

    /**
     * 보상 이벤트를 비동기 방식으로 발행합니다.
     * 성능이 중요한 경우 사용할 수 있습니다.
     */
    public void sendCompensationAsync(CompensationEvent event) {
        try {
            BalanceCompensationEvent payload = mapToPayload(event);

            // 비동기 방식 발행
            CompletableFuture<SendResult<String, BalanceCompensationEvent>> future = kafkaTemplate.send(COMPENSATION_TOPIC, payload.eventId(), payload);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[COMPENSATION-SENT] Successfully published - eventId={}, partition={}", payload.eventId(), result.getRecordMetadata().partition());
                } else {
                    log.error("[COMPENSATION-FAILED] Failed to publish - eventId={}, error={}", payload.eventId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("[COMPENSATION-FAILED] Failed to serialize event - eventId={}, error={}", event.eventId(), e.getMessage());
            throw new CompensationPublishException("Failed to serialize compensation event", e);
        }
    }

    /**
     * 보상 이벤트 발행 실패 예외
     */
    public static class CompensationPublishException extends RuntimeException {
        public CompensationPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}