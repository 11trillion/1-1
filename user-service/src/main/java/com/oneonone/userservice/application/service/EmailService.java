package com.oneonone.userservice.application.service;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.infrastructure.mail.MailService;
import com.oneonone.userservice.infrastructure.mail.RedisService;
import com.oneonone.userservice.presentation.dto.response.EmailVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final MailService mailService;
    private final RedisService redisService;

    public void sendCode(String email) {
        String code = mailService.generateCode();
        mailService.send(email, code);
        redisService.setCode(email, code);
    }

    public EmailVerifyResponse verifyCode(String email, String code) {
        String saved = redisService.getCode(email);
        if (!saved.equals(code)) throw new BusinessException(UserErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        return EmailVerifyResponse.from(code);
    }
}