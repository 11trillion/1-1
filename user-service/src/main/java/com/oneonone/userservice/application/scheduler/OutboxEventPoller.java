package com.oneonone.userservice.application.scheduler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.infrastructure.kafka.UserKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPoller {

    private static final int BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;
    private final UserKafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    /**
     * 일정 주기로 처리되지 않은 Outbox 이벤트를 조회하여 Kafka로 발행한다.
     * - Kafka 발행 성공 시에만 processed=true 처리
     * - 실패 시 재시도를 위해 processed=false 유지
     */
    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> events =
                outboxRepository.findUnprocessedEvents(BATCH_SIZE);

        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            if (!event.canRetry()) {
                event.markAsFailed();
                outboxRepository.save(event);
                log.error("[OUTBOX-POLLER] Max retries exceeded: eventId = {}, userId = {}", event.getEventId(), event.getUserId());
                return;
            }
            try {
                // send(비동기) 쓰면 응답 기다리지 않고 바로 처리 -> 유실 가능 BUT 대부분 성공
                // sendSync(동기) 쓰면 응답 -> 신뢰성 있는 메시지 전송 가능 BUT 느림
                // 포인트(중요 정보)라 동기 처리 해야할 것 같긴 한데 일단 성능 비교 해봣을 때 크게 차이 안 나면 그냥 sendSync로 진행해도 될 것 같아요
                kafkaProducer.sendSync(event.toBalanceEventPayload(objectMapper));
                event.markAsSuccess();
                log.info(
                        "[OUTBOX-SENT] eventId={}, userId={}",
                        event.getEventId(),
                        event.getUserId()
                );
                outboxRepository.save(event);
            } catch (Exception e) {
                // ❗ 실패 시 processed=false 유지 (재시도 대상)
                log.error(
                        "[OUTBOX-FAILED] eventId={}, reason={}",
                        event.getEventId(),
                        e.getMessage(),
                        e
                );
                event.increaseRetry();
                outboxRepository.save(event);
            }
        }
    }
}