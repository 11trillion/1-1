package com.oneonone.userservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.userservice.infrastructure.kafka.event.BettingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DltNotificationReceiver {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${slack.web-hook-url}")
    private String SLACK_WEBHOOK_URL;

    @KafkaListener(
            topics = "betting-reward.DLT",
            groupId = "user-service",
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void handleDlt(BettingEvent event) {
        try {
            log.error("Moved to DLT: {}", event);
            sendToSlack(event);
        } catch (Exception e) {
            log.error("Error processing dlt: {}", e.getMessage());
        }
    }

    public void sendToSlack(BettingEvent event) {
        try {
            String notification = objectMapper.writeValueAsString(event);
            String message = String.format("""
                    !!! DLT Message Detected
                    Details: ```%s```
                    """, notification);
            String payload = objectMapper.writeValueAsString(new SlackPayload(message));
            restTemplate.postForObject(SLACK_WEBHOOK_URL, payload, String.class);
            log.info("Processed sending to slack: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send message: {}", e.getMessage(), e);
        }
    }

    private static class SlackPayload {
        private final String text;

        public SlackPayload(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
