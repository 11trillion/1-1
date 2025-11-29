package com.oneonone.apigateway.filter;

import com.oneonone.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
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

    public AuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(JwtTokenProvider.BEARER_PREFIX)) return onError(exchange, "Missing or invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            try {
                String token = jwtTokenProvider.substringToken(authHeader);
                if (!jwtTokenProvider.validateToken(token)) return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                Claims claims = jwtTokenProvider.getClaims(token);
                String userId = claims.getSubject();
                String role = claims.get(JwtTokenProvider.AUTHORIZATION_KEY, String.class);
                ServerHttpRequest newRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role)
                        .build();
                ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
                return chain.filter(newExchange);
            } catch (Exception e) {
                log.error("JWT processing error: {}", e.getMessage());
                return onError(exchange, "JWT processing failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        log.error("Authentication error: {}", message);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}
