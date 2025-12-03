package com.oneonone.gameservice.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.gameservice.application.event.GameCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    //betting의 주소
    @Value("${kafka.topics.game-completed:game-completed-events}")
    private String topicName;

    public void publishGameCompleted(GameCompletedEvent gameCompletedEvent) {
        log.info("Publishing game completed event to kafka, gamdId = {}", gameCompletedEvent.gameId());

        try{
            String payload = objectMapper.writeValueAsString(gameCompletedEvent);
            kafkaTemplate
                    .send(topicName, gameCompletedEvent.gameId().toString(), payload)
                    .whenComplete( (result,error) -> {
                        if(error != null) {
                            log.error("Publishing game completed event to kafka failed", error);
                        } else {
                            log.info("Publishing game completed event to kafka completed, topic ={}, offset= {}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("gameCompletedEvent failed to serialize",e);
        }
    }
}
