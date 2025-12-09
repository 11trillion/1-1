package com.oneonone.userservice.application.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceCompensationEvent;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceEvent;
import com.oneonone.userservice.infrastructure.kafka.producer.UserKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxRepository outboxRepository;
    private final UserKafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    /**
     * 개별 이벤트를 처리하는 메서드
     * 각 이벤트마다 별도 트랜잭션으로 처리하여 일부 실패가 전체에 영향을 주지 않도록 함
     * @Transactional이 프록시를 통해 정상 작동함
     */
    @Transactional
    public void processEvent(OutboxEvent event) {
        // 재시도 가능 여부 확인
        if (!event.canRetry()) {
            event.markAsFailed();
            outboxRepository.save(event);
            compensateEvent(event);
            throw new MaxRetriesExceededException(
                    "Max retries exceeded for eventId: " + event.getEventId()
            );
        }

        try {
            // Kafka로 이벤트 발행 (동기 방식)
            // send(비동기) 쓰면 응답 기다리지 않고 바로 처리 -> 유실 가능 BUT 대부분 성공
            // sendSync(동기) 쓰면 응답 -> 신뢰성 있는 메시지 전송 가능 BUT 느림
            // 포인트(중요 정보)라 동기 처리 해야할 것 같긴 한데 일단 성능 비교 해봣을 때 크게 차이 안 나면 그냥 sendSync로 진행해도 될 것 같아요
            BalanceEvent balanceEvent = event.toBalanceEventPayload(objectMapper);
            kafkaProducer.sendSync(balanceEvent);

            // 발행 성공 시 상태 업데이트
            event.markAsSuccess();
            outboxRepository.save(event);

            log.info("[OUTBOX-SENT] Successfully published - eventId={}, userId={}",
                    event.getEventId(), event.getUserId());

        } catch (Exception e) {
            // 발행 실패 시 재시도 카운트 증가
            event.increaseRetry();
            outboxRepository.save(event);

            log.warn("[OUTBOX-FAILED] Failed to publish (retry: {}/{}) - eventId={}, reason={}",
                    event.getRetryCount(), 3, event.getEventId(), e.getMessage());
        }
    }

    /**
     * 보상 트랜잭션 - 최대 재시도 초과 시 Balance 롤백
     */
    public void compensateEvent(OutboxEvent event) {
        try {
            BalanceEvent originalEvent = event.toBalanceEventPayload(objectMapper);

            // 새로운 eventId로 보상 이벤트 생성 (중복 방지)
            BalanceEvent compensationEvent = new BalanceEvent(
                    originalEvent.sagaId(),           // 동일한 sagaId (같은 Saga)
                    UUID.randomUUID().toString(),     // 새로운 eventId (다른 메시지)
                    originalEvent.userId(),
                    originalEvent.amount(),
                    originalEvent.type(),
                    originalEvent.betId()
            );

            kafkaProducer.sendSync(compensationEvent);
            log.info("[OUTBOX-COMPENSATE] Compensation event sent - sagaId={}, originalEventId={}, newEventId={}, userId={}",
                    event.getSagaId(), event.getEventId(), compensationEvent.eventId(), event.getUserId());
        } catch (Exception e) {
            log.error("[OUTBOX-COMPENSATE] Failed to send compensation event - sagaId={}, eventId={}, error={}",
                    event.getSagaId(), event.getEventId(), e.getMessage(), e);
        }
    }

    /**
     * 최대 재시도 횟수 초과 예외
     */
    public static class MaxRetriesExceededException extends RuntimeException {
        public MaxRetriesExceededException(String message) {
            super(message);
        }
    }
}