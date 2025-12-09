package com.oneonone.pointservice.infrastructure.security;

import com.oneonone.common.security.SecurityConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableMethodSecurity
public class PointSecurityConfig implements SecurityConfigurer {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated());
    }
}