package com.oneonone.userservice.infrastructure.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class InternalServiceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 내부 API 경로인 경우
        if (path.startsWith("/api/v1/internal/")) {
            String serviceName = request.getHeader("X-Service-Name");
            String internalRequest = request.getHeader("X-Internal-Request");

            // 허용된 서비스인지 확인
            if ("betting-service".equals(serviceName) && "true".equals(internalRequest)) {
                // 인증 통과 처리
                SecurityContextHolder.getContext().setAuthentication(
                        new PreAuthenticatedAuthenticationToken("internal-service", null,
                                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL")))
                );
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}