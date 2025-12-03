package com.oneonone.userservice.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.userservice.infrastructure.kafka.dto.BalanceEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final KafkaTemplate<String, BalanceEventPayload> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 비동기 전송
    public void send(BalanceEventPayload payload) {
            kafkaTemplate.send(TOPIC, payload.userId().toString(), payload);
            log.debug("[KAFKA-SEND] topic={}, key={}", TOPIC, payload.userId());

    }

    public void sendSync(BalanceEventPayload payload) throws ExecutionException, InterruptedException, TimeoutException {
            kafkaTemplate.send(TOPIC, payload.userId().toString(), payload)
                    .get(5, TimeUnit.SECONDS);
            log.debug("[KAFKA-SEND-SYNC] topic={}, userId={}", TOPIC, payload.userId());
    }
}