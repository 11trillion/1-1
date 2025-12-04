package com.oneonone.userservice.application.scheduler;


import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPoller {
    private static final int BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;
    private final OutboxEventProcessor processor;

    /**
     * 일정 주기로 처리되지 않은 Outbox 이벤트를 조회하여 Kafka로 발행한다.
     * - Kafka 발행 성공 시에만 processed=true 처리
     * - 실패 시 재시도를 위해 processed=false 유지
     * - 실패 시 재시도를 위해 PENDING 유지 및 retryCount 증가
     * - 최대 재시도 횟수 초과 시 FAILED 처리
     */
    @Scheduled(fixedDelay = 5000) // 5초마다 실행
    public void pollAndPublish() {
        List<OutboxEvent> events = outboxRepository.findUnprocessedEvents(BATCH_SIZE);

        if (events.isEmpty()) {
            return;
        }
        log.info("[OUTBOX-POLLER] Found {} unprocessed events", events.size());

        int successCount = 0;
        int failedCount = 0;
        int maxRetriesCount = 0;

        for (OutboxEvent event : events) {
            try {
                processor.processEvent(event); successCount++;
            } catch (OutboxEventProcessor.MaxRetriesExceededException e) {
                maxRetriesCount++;
                log.error("[OUTBOX-POLLER] Max retries exceeded: eventId={}, userId={}", event.getEventId(), event.getUserId());
                processor.compensateEvent(event);
            }
        }

        log.info("[OUTBOX-POLLER] Completed - Success: {}, Failed: {}, MaxRetries: {}", successCount, failedCount, maxRetriesCount);
    }
}