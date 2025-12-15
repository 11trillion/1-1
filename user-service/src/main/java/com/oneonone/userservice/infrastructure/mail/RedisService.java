package com.oneonone.userservice.infrastructure.mail;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public void setCode(String email, String code) {
        redisTemplate.opsForValue().set(email, code, 300, TimeUnit.SECONDS);
    }

    public String getCode(String email) {
        String code = redisTemplate.opsForValue().get(email);
        if (code == null) throw new BusinessException(UserErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        return code;
    }
}
