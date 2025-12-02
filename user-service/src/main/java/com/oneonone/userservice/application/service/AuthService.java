package com.oneonone.userservice.application.service;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.security.JwtTokenProvider;
import com.oneonone.userservice.application.command.LoginCommand;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.RefreshTokenRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.presentation.dto.response.LoginResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh.expiration}")
    private long expiration;

    public LoginResponse login(LoginCommand command) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(command.username())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(command.password(), user.getPassword())) throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        String accessToken = jwtTokenProvider.createToken(
                user.getUserId(),
                user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        String token = refreshToken.substring(7);
        refreshTokenRepository.save(user.getUserId(), token, expiration);
        return LoginResponse.from(accessToken, refreshToken);
    }

    public LoginResponse reissue(String token) {
        if (!jwtTokenProvider.validateToken(token)) throw new BusinessException(UserErrorCode.INVALID_REFRESH_TOKEN);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String refreshToken = refreshTokenRepository.findByUserId(userId);
        if (!token.equals(refreshToken)) throw new BusinessException(UserErrorCode.INVALID_REFRESH_TOKEN);
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        String newAccessToken = jwtTokenProvider.createToken(user.getUserId(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        refreshTokenRepository.save(user.getUserId(), newRefreshToken.substring(7), expiration);
        return LoginResponse.from(newAccessToken, newRefreshToken);
    }

    public void logout(String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        refreshTokenRepository.deleteByUserId(userId);
        long time = getExpiration(token);
        refreshTokenRepository.addToBlacklist(token, time);
    }

    private long getExpiration(String token) {
        Claims claims = jwtTokenProvider.getClaims(token);
        long diff = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(0, diff);
    }
}
