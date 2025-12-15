package com.oneonone.userservice.infrastructure.config;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceCompensationEvent;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceEvent;
import com.oneonone.userservice.infrastructure.kafka.event.BettingEvent;
import com.oneonone.userservice.infrastructure.kafka.event.CompensationResultEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    // DLT Producer
    @Bean
    public ProducerFactory<String, Object> dltProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

    // DLT Consumer
    @Bean
    public ConsumerFactory<String, BettingEvent> dltConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service.dlt");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.oneonone.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BettingEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BettingEvent> dltKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BettingEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dltConsumerFactory());
        return factory;
    }

    // BettingEvent Consumer
    @Bean
    public ConsumerFactory<String, BettingEvent> bettingEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.oneonone.*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BettingEvent.class.getName());
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BettingEvent> bettingEventConcurrentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BettingEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bettingEventConsumerFactory());
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(dltKafkaTemplate()),
                new FixedBackOff(1000L, 3L)
        );
        errorHandler.addNotRetryableExceptions(BusinessException.class);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    // BalanceEvent Producer
    @Bean
    public ProducerFactory<String, BalanceEvent> balanceEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, BalanceEvent> balanceEventKafkaTemplate() {
        return new KafkaTemplate<>(balanceEventProducerFactory());
    }

    // BalanceCompensationEvent Consumer
    @Bean
    public ConsumerFactory<String, BalanceCompensationEvent> balanceCompensationEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.oneonone.*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BalanceCompensationEvent.class.getName());
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BalanceCompensationEvent> balanceCompensationEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BalanceCompensationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(balanceCompensationEventConsumerFactory());
        return factory;
    }

    // CompensationResultEvent Producer
    @Bean
    public ProducerFactory<String, CompensationResultEvent> compensationResultEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, CompensationResultEvent> compensationResultEventKafkaTemplate() {
        return new KafkaTemplate<>(compensationResultEventProducerFactory());
    }
}