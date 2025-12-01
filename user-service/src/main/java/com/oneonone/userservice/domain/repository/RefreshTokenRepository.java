package com.oneonone.userservice.domain.repository;

public interface RefreshTokenRepository {
    void save(Long userId, String refreshToken, Long expiration);

    String findByUserId(Long userId);
}
