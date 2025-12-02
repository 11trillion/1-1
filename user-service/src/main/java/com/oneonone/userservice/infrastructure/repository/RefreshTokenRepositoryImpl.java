package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "RefreshToken: ";
    private static final String BLACKLIST = "Blacklist: ";

    @Override
    public void save(Long userId, String refreshToken, Long expiration) {
        redisTemplate.opsForValue().set(
                PREFIX + userId.toString(),
                refreshToken,
                expiration,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public String findByUserId(Long userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }

    @Override
    public void addToBlacklist(String token, long time) {
        redisTemplate.opsForValue().set(
                BLACKLIST + token,
                "logout",
                time,
                TimeUnit.MILLISECONDS
        );
    }
}
