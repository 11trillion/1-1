package com.oneonone.userservice.infrastructure.kafka.producer;

import com.oneonone.userservice.infrastructure.kafka.event.BalanceEvent;
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

    private final KafkaTemplate<String, BalanceEvent> kafkaTemplate;

    // 비동기 전송
    public void send(BalanceEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.userId().toString(), event);
            log.debug("[KAFKA-SEND] topic={}, key={}", TOPIC, event.userId());
        } catch (Exception e) {
            log.error("[KAFKA-SEND] Failed to send message - userId={}", event.userId(), e);
        }
    }

    public void sendSync(BalanceEvent event) throws ExecutionException, InterruptedException, TimeoutException {
        kafkaTemplate.send(TOPIC, event.userId().toString(), event)
                .get(5, TimeUnit.SECONDS);
        log.debug("[KAFKA-SEND-SYNC] topic={}, userId={}, event={}", TOPIC, event.userId(), event);
    }
}