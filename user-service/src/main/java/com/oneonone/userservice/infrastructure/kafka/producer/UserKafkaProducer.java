package com.oneonone.userservice.infrastructure.kafka.producer;

import com.oneonone.common.infrastructure.kafka.BalanceEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class UserKafkaProducer {

    private static final String TOPIC = "point-update-event";

    private final KafkaTemplate<String, BalanceEventPayload> kafkaTemplate;

    // 생성자에서 @Qualifier 지정
    @Autowired
    public UserKafkaProducer( KafkaTemplate<String, BalanceEventPayload> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

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