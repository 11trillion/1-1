package com.oneonone.bettingservice.infrastructure;

import com.oneonone.bettingservice.infrastructure.event.BettingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile("load-test")
@RequiredArgsConstructor
public class BettingLoadTest implements CommandLineRunner {

    private final KafkaTemplate<String, BettingEvent> kafkaTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Betting reward test started.");
        for (int i = 0; i < 1000; i++) {
            Long userId = (long) (i % 20) + 1; // 유저 분산
            BettingEvent event = new BettingEvent(
                    UUID.randomUUID().toString(), // sagaId
                    UUID.randomUUID().toString(), // eventId
                    userId,
                    1000L,
                    UUID.randomUUID().toString(), // bettingId
                    System.currentTimeMillis()
            );
            kafkaTemplate.send(
                    "betting-reward",
                    userId.toString(),
                    event
            );
        }
        log.info("Betting reward test finished.");
    }
}
