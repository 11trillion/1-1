package com.oneonone.pointservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointType;
import com.oneonone.pointservice.domain.repository.PointRepository;
import com.oneonone.pointservice.infrastructure.kafka.dto.BalanceEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// class 이름은 발생한 event를 따라감
@Component
@RequiredArgsConstructor
public class BalanceEventConsumer {
    private final PointRepository pointRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "point-update-event",
            groupId = "point-service"
    )

    @Transactional
    public void consume(String payloadJson){
        try {
            // Json 역직렬화
            BalanceEventPayload payload = objectMapper.readValue(payloadJson, BalanceEventPayload.class);

            // 중복 이벤트 처리 방지
            if (pointRepository.existsByEventId(payload.eventId())) {
                return;
            }

            // 타입 검증
            PointType pointType;
            try {
                pointType = PointType.valueOf(payload.type());
            } catch (IllegalArgumentException e) {
                // 잘못된 type 들어왔을 때 로깅 후 종료
                System.err.println("Invalid PointType: " + payload.type());
                return;
            }

            // 포인트 생성 후 저장
            Point point = Point.create(
                    payload.eventId(),
                    payload.userId(),
                    payload.amount(),
                    pointType,
                    payload.description()
            );
            pointRepository.save(point);
        } catch (Exception e) {
            System.err.println("Failed to consume payload: " + e.getMessage());
        }
    }
}