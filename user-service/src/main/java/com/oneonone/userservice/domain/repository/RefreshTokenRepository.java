package com.oneonone.userservice.domain.repository;

public interface RefreshTokenRepository {
    void save(Long userId, String refreshToken, Long expiration);

    String findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void addToBlacklist(String token, long time);
}
