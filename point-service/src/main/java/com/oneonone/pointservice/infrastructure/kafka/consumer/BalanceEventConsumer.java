package com.oneonone.pointservice.infrastructure.kafka.consumer;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.repository.PointRepository;
import com.oneonone.pointservice.infrastructure.kafka.event.BalanceEvent;
import com.oneonone.pointservice.infrastructure.kafka.event.CompensationEvent;
import com.oneonone.pointservice.infrastructure.kafka.event.CompensationResultEvent;
import com.oneonone.pointservice.infrastructure.kafka.producer.CompensationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// class 이름은 발생한 event를 따라감
@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceEventConsumer {
    private final PointRepository pointRepository;
    private final CompensationProducer compensationProducer;

    @KafkaListener(
            topics = "point-update-event",
            groupId = "point-service",
            containerFactory = "balanceEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(BalanceEvent event) {
        try {
            // 중복 이벤트 처리 방지
            // 멱등성 체크
            if (pointRepository.existsByEventId(UUID.fromString(event.eventId()))) {
                log.warn("[KAFKA-CONSUME] Duplicate event - eventId={}", event.eventId());
                return;
            }

            // 포인트 생성 후 저장
            Point point = Point.create(
                    event.sagaId(),
                    event.eventId(),
                    event.userId(),
                    event.amount(),
                    event.type(),
                    event.betId()
            );
            // TODO: 보상 트랜잭션 테스트
            if (event.amount() == 9999L) {
                throw new BusinessException(PointErrorCode.FORCE);
            }
            pointRepository.save(point);
            point.markSuccess();
            log.info("[KAFKA-CONSUME] Point saved successfully - eventId={}, userId={}, amount={}", event.eventId(), event.userId(), event.amount());
        } catch (DuplicateKeyException e) {
            // 중복 키 오류 → 이미 처리됨 → 무시
            log.warn("[KAFKA-CONSUME] Duplicate key detected - eventId={}",
                    event != null ? event.eventId() : "unknown");
            // 예외를 던지지 않음 (정상 처리로 간주)
        } catch (TransientDataAccessException e) {
            // 일시적 DB 장애 → Kafka 재시도
            log.error("[KAFKA-CONSUME] Transient DB error - will retry - eventId={}, error={}",
                    event != null ? event.eventId() : "unknown", e.getMessage());
            throw e; // Kafka가 자동 재시도
        } catch (DataAccessException e) {
            // 심각한 DB 오류 → Kafka 재시도 후 DLQ
            log.error("[KAFKA-CONSUME] DB error - will retry - eventId={}, error={}",
                    event != null ? event.eventId() : "unknown", e.getMessage());
            throw e; // Kafka가 자동 재시도
        } catch (BusinessException e) {
            // 비즈니스 로직 오류 → 보상 트랜잭션
            log.error("[KAFKA-CONSUME] Business error - triggering compensation - eventId={}, error={}",
                    event != null ? event.eventId() : "unknown", e.getMessage());
            if (event != null) {
                compensate(event, e.getMessage());
            }
            // 예외를 던지지 않음 (재시도 불필요)
        } catch (Exception e) {
            // 알 수 없는 오류 → Kafka 재시도 후 DLQ
            log.error("[KAFKA-CONSUME] Unknown error - will retry - eventId={}, error={}",
                    event != null ? event.eventId() : "unknown", e.getMessage(), e);
            throw e; // Kafka가 자동 재시도
        }
    }
    /**
     * 보상 트랜잭션 이벤트를 발행합니다.
     * Point 저장 실패 시 UserService에 Balance 복구를 요청
     */
    private void compensate(BalanceEvent event, String error) {
        try {
            log.info("[SAGA] Starting compensation - eventId={}, userId={}, error={}",
                    event.eventId(), event.userId(), error);

            // Point 조회 및 보상 상태로 변경
            Point point = pointRepository.findByEventId(UUID.fromString(event.eventId()))
                    .orElseGet(() -> {
                        Point newPoint = Point.create(
                                event.sagaId(),
                                event.eventId(),
                                event.userId(),
                                event.amount(),
                                event.type(),
                                event.betId()
                        );
                        pointRepository.save(newPoint);
                        log.info("[SAGA] Point created for compensation - eventId={}", event.eventId());
                        return newPoint;
                    });
            // TODO: 보상 시작 (상태를 COMPENSATED로 변경. COMPENSATING -> 이벤트 수신 후 COMPENSATED로 변경 리팩토링 필요)
            point.startCompensation(error);
            log.info("[SAGA] Point compensation started - pointId={}, eventId={}",
                    point.getId(), event.eventId());

            CompensationEvent compensationEvent = new CompensationEvent(
                    event.sagaId(),
                    event.eventId(),
                    event.userId(),
                    event.amount(),
                    event.type(),
                    event.betId(),
                    error
            );

            // 보상 이벤트 발행 (동기 방식 권장)

            compensationProducer.sendCompensation(compensationEvent);
            log.info("[SAGA] Compensation event published - eventId={}, userId={}",
                    event.eventId(), event.userId());
        } catch (Exception e) {
            // 보상 이벤트 발행 실패는 심각한 문제!
            log.error("[SAGA] CRITICAL - Failed to publish compensation event - eventId={}, error={}",
                    event.eventId(), e.getMessage(), e); // TODO: 알림 발송, 별도 테이블에 기록 등 추가 처리 필요
            // 예: alertService.sendCriticalAlert("Compensation failed", payload);
        }
    }

    @KafkaListener(
            topics = "balance-compensation-result",
            groupId = "point-service",
            containerFactory = "compensationResultEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeCompensationResult(CompensationResultEvent event) {
        try {
            log.info("[KAFKA-CONSUME] [Compensation Result] Received - eventId={}, success={}",
                    event.eventId(), event.success());

            // Point 조회
            Point point = pointRepository.findByEventId(UUID.fromString(event.eventId()))
                    .orElseThrow(() -> new BusinessException(PointErrorCode.POINT_NOT_FOUND));

            // 보상 결과에 따라 상태 변경
            if (event.success()) {
                // UserService에서 Balance 롤백 성공
                point.markCompensated();
                log.info("[KAFKA-CONSUME] [Compensation Result] Point compensated - pointId={}, eventId={}",
                        point.getId(), event.eventId());
            } else {
                // UserService에서 Balance 롤백 실패 (거의 발생하지 않음)
                point.failCompensation();
                log.error("[KAFKA-CONSUME] [Compensation Result] Compensation failed - pointId={}, reason={}",
                        point.getId(), event.failureReason());

                // TODO: 관리자 알림, 수동 처리 필요
            }

        } catch (BusinessException e) {
            log.error("[KAFKA-CONSUME] [Compensation Result] Business error - eventId={}, error={}",
                    event != null ? event.eventId() : "unknown", e.getMessage());
            // 재시도하지 않음 (비즈니스 로직 오류)

        } catch (Exception e) {
            log.error("[KAFKA-CONSUME] [Compensation Result] Unexpected error - eventId={}",
                    event != null ? event.eventId() : "unknown", e);
            throw e; // Kafka 재시도
        }
    }
}