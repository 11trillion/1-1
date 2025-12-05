package com.oneonone.userservice.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 보상 완료 결과를 PointService에 알림
     */
    public void sendCompensationResult(CompensationResultEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            SendResult<String, String> result = kafkaTemplate
                    .send(TOPIC, event.eventId(), payload)
                    .get(); // 동기 방식 (신뢰성 중요)

            log.info("[COMPENSATION-RESULT-SENT] Successfully published - eventId={}, success={}, partition={}",
                    event.eventId(),
                    event.success(),
                    result.getRecordMetadata().partition());

        } catch (Exception e) {
            log.error("[COMPENSATION-RESULT-FAILED] Failed to publish - eventId={}, error={}",
                    event.eventId(), e.getMessage(), e);
            // 실패 시 재시도 또는 알림 로직 추가 가능
            throw new RuntimeException("Failed to publish compensation result", e);
        }
    }
}
