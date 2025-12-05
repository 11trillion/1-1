package com.oneonone.pointservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.infrastructure.kafka.BalanceEventPayload;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.repository.PointRepository;
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
    private final ObjectMapper objectMapper;
    private final CompensationProducer compensationProducer;

    @KafkaListener(
            topics = "point-update-event",
            groupId = "point-service"
    )
    @Transactional
    public void consume(String payloadJson) {
        BalanceEventPayload payload = null;
        try {
            // Json 역직렬화
            payload = objectMapper.readValue(payloadJson, BalanceEventPayload.class);

            // 중복 이벤트 처리 방지
            // 멱등성 체크
            if (pointRepository.existsByEventId(UUID.fromString(payload.eventId()))) { log.warn("[KAFKA-CONSUME] Duplicate event - eventId={}", payload.eventId());
                return;
            }

            // 포인트 생성 후 저장
            Point point = Point.create(
                    payload.eventId(),
                    payload.userId(),
                    payload.amount(),
                    payload.type(),
                    payload.betId()
            );
            // TODO: 보상 트랜잭션 테스트
            if (payload.amount() == 9999L) {
                throw new BusinessException(PointErrorCode.FORCE);
            }
            pointRepository.save(point);
            point.markSuccess();
            log.info("[KAFKA-CONSUME] Point saved successfully - eventId={}, userId={}, amount={}", payload.eventId(), payload.userId(), payload.amount());
        } catch (JsonProcessingException e) {
            // JSON 파싱 오류 → 재시도해도 소용없음 → 보상 처리
            log.error("[KAFKA-CONSUME] JSON parsing error - eventId={}, error={}",
                    payload != null ? payload.eventId() : "unknown", e.getMessage());
            if (payload != null) {
                compensate(payload, "Invalid JSON format: " + e.getMessage());
            }
            // 예외를 던지지 않음 (재시도 불필요)
        } catch (DuplicateKeyException e) {
            // 중복 키 오류 → 이미 처리됨 → 무시
            log.warn("[KAFKA-CONSUME] Duplicate key detected - eventId={}",
                    payload != null ? payload.eventId() : "unknown");
            // 예외를 던지지 않음 (정상 처리로 간주)
        } catch (TransientDataAccessException e) {
            // 일시적 DB 장애 → Kafka 재시도
            log.error("[KAFKA-CONSUME] Transient DB error - will retry - eventId={}, error={}",
                    payload != null ? payload.eventId() : "unknown", e.getMessage());
            throw e; // Kafka가 자동 재시도
        } catch (DataAccessException e) {
            // 심각한 DB 오류 → Kafka 재시도 후 DLQ
            log.error("[KAFKA-CONSUME] DB error - will retry - eventId={}, error={}",
                    payload != null ? payload.eventId() : "unknown", e.getMessage());
            throw e; // Kafka가 자동 재시도
        } catch (BusinessException e) {
            // 비즈니스 로직 오류 → 보상 트랜잭션
            log.error("[KAFKA-CONSUME] Business error - triggering compensation - eventId={}, error={}",
                    payload != null ? payload.eventId() : "unknown", e.getMessage());
            if (payload != null) {
                compensate(payload, e.getMessage());
            }
            // 예외를 던지지 않음 (재시도 불필요)
        } catch (Exception e) {
            // 알 수 없는 오류 → Kafka 재시도 후 DLQ
            log.error("[KAFKA-CONSUME] Unknown error - will retry - eventId={}, error={}",
                    payload != null ? payload.eventId() : "unknown", e.getMessage(), e);
            throw e; // Kafka가 자동 재시도
        }
    }
    /**
     * 보상 트랜잭션 이벤트를 발행합니다.
     * Point 저장 실패 시 UserService에 Balance 복구를 요청
     */
    private void compensate(BalanceEventPayload payload, String error) {
        try {
            log.info("[SAGA] Starting compensation - eventId={}, userId={}, error={}",
                    payload.eventId(), payload.userId(), error);

            // Point 조회 및 보상 상태로 변경
            Point point = pointRepository.findByEventId(UUID.fromString(payload.eventId()))
                    .orElseGet(() -> {
                        Point newPoint = Point.create(
                                payload.eventId(),
                                payload.userId(),
                                payload.amount(),
                                payload.type(),
                                payload.betId()
                        );
                        pointRepository.save(newPoint);
                        log.info("[SAGA] Point created for compensation - eventId={}", payload.eventId());
                        return newPoint;
                    });
            // 보상 시작 (상태를 COMPENSATING으로 변경)
            point.startCompensation(error);
            log.info("[SAGA] Point compensation started - pointId={}, eventId={}",
                    point.getId(), payload.eventId());

            CompensationEvent event = new CompensationEvent(
                    payload.eventId(),
                    payload.userId(),
                    payload.amount(),
                    payload.type(),
                    payload.betId(),
                    error
            );

            // 보상 이벤트 발행 (동기 방식 권장)

            compensationProducer.sendCompensation(event);
            log.info("[SAGA] Compensation event published - eventId={}, userId={}",
                    payload.eventId(), payload.userId());
        } catch (Exception e) {
            // 보상 이벤트 발행 실패는 심각한 문제!
            log.error("[SAGA] CRITICAL - Failed to publish compensation event - eventId={}, error={}",
                    payload.eventId(), e.getMessage(), e); // TODO: 알림 발송, 별도 테이블에 기록 등 추가 처리 필요
            // 예: alertService.sendCriticalAlert("Compensation failed", payload);
        }
    }

    @KafkaListener(
            topics = "balance-compensation-result",
            groupId = "point-service"
    )
    @Transactional
    public void consumeCompensationResult(String payloadJson) {
        CompensationResultEvent event = null;

        try {
            // JSON 역직렬화
            event = objectMapper.readValue(payloadJson, CompensationResultEvent.class);

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

        } catch (JsonProcessingException e) {
            log.error("[KAFKA-CONSUME] [Compensation Result] JSON parsing error - eventId={}",
                    event != null ? event.eventId() : "unknown", e);
            // 재시도하지 않음 (파싱 오류는 재시도해도 실패)

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