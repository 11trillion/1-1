package com.oneonone.common.infrastructure.config;

import com.oneonone.common.infrastructure.kafka.BalanceCompensationEventPayload;
import com.oneonone.common.infrastructure.kafka.BalanceEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConfig {
    // Producer 설정
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Listener 설정
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        // 컨슈머 팩토리 설정을 위한 맵을 생성합니다.
        Map<String, Object> configProps = new HashMap<>();
        // Kafka 브로커의 주소를 설정합니다.
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        // 메시지 키의 디시리얼라이저 클래스를 설정합니다.
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 메시지 값의 디시리얼라이저 클래스를 설정합니다.
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 설정된 프로퍼티로 DefaultKafkaConsumerFactory를 생성하여 반환합니다.
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    // Kafka 리스너 컨테이너 팩토리를 생성하는 빈을 정의합니다.
    // ConcurrentKafkaListenerContainerFactory는 Kafka 메시지를 비동기적으로 수신하는 리스너 컨테이너를 생성하는 데 사용됩니다.
    // 이 팩토리는 @KafkaListener 어노테이션이 붙은 메서드들을 실행할 컨테이너를 제공합니다.
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        // ConcurrentKafkaListenerContainerFactory를 생성합니다.
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 컨슈머 팩토리를 리스너 컨테이너 팩토리에 설정합니다.
        factory.setConsumerFactory(consumerFactory());
        // 설정된 리스너 컨테이너 팩토리를 반환합니다.
        return factory;
    }

    // ---------------------------
    // BalanceEventPayload
    // Template & Producer
    // ---------------------------
    @Bean
    @Qualifier("balanceKafkaTemplate")
    public KafkaTemplate<String, BalanceEventPayload> balanceKafkaTemplate() {
        return new KafkaTemplate<>(balanceProducerFactory());
    }

    @Bean
    public ProducerFactory<String, BalanceEventPayload> balanceProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<String, BalanceEventPayload>(configProps);
    }

    // ---------------------------
    // BalanceCompensationEventPayload
    // Template & Consumer & Listener Factory & ErrorHandler + DLQ + 재시도
    // ---------------------------
    @Bean
    @Qualifier("balanceCompensationKafkaTemplate")
    public KafkaTemplate<String, BalanceCompensationEventPayload> balanceCompensationKafkaTemplate() {
        return new KafkaTemplate<>(balanceCompensationProducerFactory());
    }

    @Bean
    public ProducerFactory<String, BalanceCompensationEventPayload> balanceCompensationProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // DTO 직렬화
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory<String, BalanceCompensationEventPayload> balanceConsumerFactory() {
        JsonDeserializer<BalanceCompensationEventPayload> deserializer =
                new JsonDeserializer<>(BalanceCompensationEventPayload.class);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BalanceCompensationEventPayload> balanceEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BalanceCompensationEventPayload> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(balanceConsumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(kafkaErrorHandler(balanceCompensationKafkaTemplate()));
        return factory;
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler(
            @Qualifier("balanceCompensationKafkaTemplate") KafkaTemplate<String, BalanceCompensationEventPayload> kafkaTemplate) {
        // DLQ 전송 설정

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (ConsumerRecord<?, ?> record, Exception ex) -> {
                    String dlqTopic = record.topic() + ".DLQ";
                    log.error("[DLQ] Sending message to DLQ - topic={}, offset={}, error={}",
                            dlqTopic, record.offset(), ex.getMessage());
                    return new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition());
                }
        );

        // Exponential Backoff 재시도 설정 (1s → 2s → 4s)
        DefaultErrorHandler errorHandler = getDefaultErrorHandler(recoverer);

        return errorHandler;
    }

    private static DefaultErrorHandler getDefaultErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // 재시도 시 로깅
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("[KAFKA-RETRY] Retry attempt {}/3 - topic={}, offset={}, error={}",
                    deliveryAttempt, record.topic(), record.offset(), ex.getMessage());
        });
        return errorHandler;
    }

    // ---------------------------
    // Listener Container Factory + ErrorHandler + DLQ
    // ---------------------------
//    @Bean(name = "stringKafkaListenerContainerFactoryWithErrorHandler")
//    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactoryWithErrorHandler(
//            KafkaTemplate<String, String> kafkaTemplate
//    ) {
//        ConcurrentKafkaListenerContainerFactory<String, String> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        factory.setConcurrency(3);
//        factory.setCommonErrorHandler(kafkaErrorHandler(kafkaTemplate));
//        return factory;
//    }
}
