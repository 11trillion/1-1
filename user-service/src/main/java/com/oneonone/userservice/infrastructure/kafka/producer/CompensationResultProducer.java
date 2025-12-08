package com.oneonone.userservice.infrastructure.kafka.producer;

import com.oneonone.userservice.infrastructure.kafka.event.CompensationResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationResultProducer {

    private static final String TOPIC = "balance-compensation-result";

    private final KafkaTemplate<String, CompensationResultEvent> kafkaTemplate;

    /**
     * 보상 완료 결과를 PointService에 알림
     */
    public void sendCompensationResult(CompensationResultEvent event) {
        try {
            SendResult<String, CompensationResultEvent> result = kafkaTemplate
                    .send(TOPIC, event.eventId(), event)
                    .get(); // 동기 방식 (신뢰성 중요)

            log.info("[COMPENSATION-RESULT-SENT] Successfully published - sagaId={}, eventId={}, success={}, partition={}",
                    event.sagaId(),
                    event.eventId(),
                    event.success(),
                    result.getRecordMetadata().partition());

        } catch (Exception e) {
            log.error("[COMPENSATION-RESULT-FAILED] Failed to publish - sagaId={}, eventId={}, error={}",
                    event.sagaId(), event.eventId(), e.getMessage(), e);
            // 실패 시 재시도 또는 알림 로직 추가 가능
            throw new RuntimeException("Failed to publish compensation result", e);
        }
    }
}
