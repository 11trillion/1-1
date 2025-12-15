package com.oneonone.userservice.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class MailService {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    public MailService(JavaMailSender javaMailSender, MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.mailProperties = mailProperties;
    }

    private static final String EMAIL_TITLE = "회원 가입을 위한 인증 이메일";
    private static final String EMAIL_CONTENT = "아래의 인증 번호를 입력하여 회원 가입을 완료해주세요." +
            "<br><br>" +
            "인증번호: %s" +
            "<br><br>인증 번호의 유효 시간은 5분입니다.";

    public void send(String email, String code) {
        String content = String.format(EMAIL_CONTENT, code);
        mailSend(mailProperties.getUsername(), email, EMAIL_TITLE, content);
    }

    public String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    public void mailSend(String setFrom, String toMail, String title, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(setFrom);
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Email send failed", e);
        }
    }
}