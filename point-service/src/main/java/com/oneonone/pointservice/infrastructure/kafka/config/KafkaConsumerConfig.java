//package com.oneonone.pointservice.infrastructure.kafka.config;
//
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.listener.CommonErrorHandler;
//import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
//import org.springframework.kafka.listener.DefaultErrorHandler;
//import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Configuration
//@RequiredArgsConstructor
//public class KafkaConsumerConfig {
//
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    private final KafkaTemplate<String, String> kafkaTemplate;
//
//    /**
//     * Kafka Consumer Factory 설정
//     */
//    @Bean
//    public ConsumerFactory<String, String> consumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  // 수동 커밋
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
//
//    /**
//     * Kafka Listener Container Factory 설정
//     * - 재시도 정책: 1초, 2초, 4초 간격으로 최대 3번 재시도
//     * - 최종 실패 시 DLQ(Dead Letter Queue)로 전송
//     */
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, String> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//
//        factory.setConsumerFactory(consumerFactory());
//
//        // 에러 핸들러 설정
//        factory.setCommonErrorHandler(errorHandler());
//
//        // 동시 처리 스레드 수
//        factory.setConcurrency(3);
//
//        return factory;
//    }
//
//    /**
//     * 에러 핸들러 설정
//     * - Exponential Backoff: 1초 → 2초 → 4초
//     * - 최대 3번 재시도
//     * - 최종 실패 시 DLQ로 전송
//     */
//    private CommonErrorHandler errorHandler() {
//        // DLQ 전송 설정
//        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
//                kafkaTemplate,
//                (record, ex) -> {
//                    // DLQ 토픽 이름: 원본토픽.DLQ
//                    String dlqTopic = record.topic() + ".DLQ";
//                    log.error("[DLQ] Sending message to DLQ - topic={}, offset={}, error={}",
//                            dlqTopic, record.offset(), ex.getMessage());
//                    return new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition());
//                }
//        );
//
//        // Exponential Backoff 설정
//        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
//        backOff.setInitialInterval(1000L);      // 첫 재시도: 1초 후
//        backOff.setMultiplier(2.0);             // 간격을 2배씩 증가
//        backOff.setMaxInterval(10000L);         // 최대 간격: 10초
//
//        // DefaultErrorHandler 생성
//        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
//
//        // 재시도하지 않을 예외 설정 (선택)
//        // errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
//
//        // 로깅 추가
//        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
//            log.warn("[KAFKA-RETRY] Retry attempt {}/3 - topic={}, offset={}, error={}",
//                    deliveryAttempt, record.topic(), record.offset(), ex.getMessage());
//        });
//
//        return errorHandler;
//    }
//}