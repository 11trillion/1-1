package com.oneonone.pointservice.infrastructure.kafka.producer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.pointservice.infrastructure.kafka.event.CompensationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationProducer {

    private static final String COMPENSATION_TOPIC = "balance-compensation-event";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 보상 이벤트를 동기 방식으로 발행합니다.
     * 발행 실패 시 예외가 발생하여 호출자가 처리할 수 있습니다.
     */
    public void sendCompensation(CompensationEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            // 동기 방식 발행 (신뢰성 중요)
            SendResult<String, String> result = kafkaTemplate
                    .send(COMPENSATION_TOPIC, event.eventId(), payload)
                    .get();  // 동기 대기

            log.info("[COMPENSATION-SENT] Successfully published - eventId={}, topic={}, partition={}",
                    event.eventId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition());

        } catch (Exception e) {
            log.error("[COMPENSATION-FAILED] Failed to publish compensation event - eventId={}, error={}",
                    event.eventId(), e.getMessage());
            throw new CompensationPublishException("Failed to publish compensation event", e);
        }
    }

    /**
     * 보상 이벤트를 비동기 방식으로 발행합니다.
     * 성능이 중요한 경우 사용할 수 있습니다.
     */
    public void sendCompensationAsync(CompensationEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            // 비동기 방식 발행
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(COMPENSATION_TOPIC, event.eventId(), payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[COMPENSATION-SENT] Successfully published - eventId={}, partition={}",
                            event.eventId(),
                            result.getRecordMetadata().partition());
                } else {
                    log.error("[COMPENSATION-FAILED] Failed to publish - eventId={}, error={}",
                            event.eventId(), ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("[COMPENSATION-FAILED] Failed to serialize event - eventId={}, error={}",
                    event.eventId(), e.getMessage());
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