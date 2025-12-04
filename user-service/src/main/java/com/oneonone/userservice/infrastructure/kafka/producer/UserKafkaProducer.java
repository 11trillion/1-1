package com.oneonone.userservice.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.infrastructure.kafka.BalanceEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserKafkaProducer {

    private static final String TOPIC = "point-update-event";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 비동기 전송
    public void send(BalanceEventPayload payload) {
        try { String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(TOPIC, payload.userId().toString(), json);
            log.debug("[KAFKA-SEND] topic={}, key={}", TOPIC, payload.userId());
        } catch (Exception e) {
            log.error("[KAFKA-SEND] Failed to send message - userId={}", payload.userId(), e);
        }
    }

    public void sendSync(BalanceEventPayload payload) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(TOPIC, payload.userId().toString(), json)
                    .get(5, TimeUnit.SECONDS); log.debug("[KAFKA-SEND-SYNC] topic={}, userId={}", TOPIC, payload.userId()); } catch (
            JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}