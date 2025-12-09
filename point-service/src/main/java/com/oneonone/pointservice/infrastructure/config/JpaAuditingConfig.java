package com.oneonone.pointservice.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "headerAuditorAware")
public class JpaAuditingConfig {
}
