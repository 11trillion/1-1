package com.oneonone.bettingservice.infrastructure.client.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("X-Service-Name", "betting-service");
            requestTemplate.header("X-Internal-Request", "true");
        };
    }

    // 사용자 헤더(X-User-Id, X-User-Role) 전달
    @Bean
    public RequestInterceptor userHeaderForwarder() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return;
            }

            var request = attrs.getRequest();
            String userId = request.getHeader("X-User-Id");
            String userRole = request.getHeader("X-User-Role");

            if (userId != null && !userId.isBlank()) {
                template.header("X-User-Id", userId);
            }
            if (userRole != null && !userRole.isBlank()) {
                template.header("X-User-Role", userRole);
            }
        };
    }
}