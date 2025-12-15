package com.oneonone.apigateway.filter;

import com.oneonone.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST = "Blacklist: ";

    public AuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // Swagger / 공개 API 화이트리스트
            if (isWhitelist(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(JwtTokenProvider.BEARER_PREFIX)) {
                return onError(exchange,
                        "Missing or invalid Authorization Header",
                        HttpStatus.UNAUTHORIZED);
            }

            try {
                String token = jwtTokenProvider.substringToken(authHeader);

                if (!jwtTokenProvider.validateToken(token)) {
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }

                return redisTemplate.hasKey(BLACKLIST + token)
                        .flatMap(isBlacklist -> {
                            if (Boolean.TRUE.equals(isBlacklist)) {
                                return onError(exchange, "Logged out token", HttpStatus.UNAUTHORIZED);
                            }

                            Claims claims = jwtTokenProvider.getClaims(token);
                            String userId = claims.getSubject();
                            String role = claims.get(JwtTokenProvider.AUTHORIZATION_KEY, String.class);

                            ServerHttpRequest newRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-User-Role", role)
                                    .build();

                            ServerWebExchange newExchange = exchange.mutate()
                                    .request(newRequest)
                                    .build();

                            return chain.filter(newExchange);
                        });
            } catch (Exception e) {
                log.error("JWT processing error: {}", e.getMessage());
                return onError(exchange, "JWT processing failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isWhitelist(String path) {
        // 필요하면 여기 더 추가하면 됨
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.startsWith("/actuator")
                || path.startsWith("/api/v1/users/signup")
                || path.startsWith("/api/v1/users/login")
                || path.startsWith("/api/v1/users/email/");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        log.error("Authentication error: {}", message);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}