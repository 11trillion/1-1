package com.oneonone.pointservice.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "headerAuditorAware")
public class JpaAuditingConfig {
}
