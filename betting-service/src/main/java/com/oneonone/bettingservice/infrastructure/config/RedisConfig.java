package com.oneonone.bettingservice.infrastructure.config;

import com.oneonone.bettingservice.domain.entity.BettingSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // BettingSession을 Redis에 쉽게 넣고 빼기 위한 설정

    @Bean
    public RedisTemplate<String, BettingSession> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, BettingSession> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // key / hashKey
        // Redis는 바이트 배열만 저장하므로, 직렬화를 해줘야함
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // 일반키 문자열로 직렬화
        redisTemplate.setKeySerializer(stringSerializer);
        // 해시구조에서의 field 이름도 문자열로 직렬화
        redisTemplate.setHashKeySerializer(stringSerializer);

        // value /hash value (JDK 직렬화 사용)
        // 값(=BettingSession 객체)을 어떤 방식으로 바이트로 바꿀지 지정하는 부분
        // JdkSerializationRedisSerializer는 자바 기본 직렬화
        Jackson2JsonRedisSerializer<BettingSession> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(BettingSession.class);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, String> bettingHashRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
