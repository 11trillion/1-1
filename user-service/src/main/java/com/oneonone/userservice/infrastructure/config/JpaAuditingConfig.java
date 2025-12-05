package com.oneonone.userservice.infrastructure.config;

import com.oneonone.common.util.HeaderAuditorAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "headerAuditorAware")
public class JpaAuditingConfig {
    public AuditorAware<Long> headerAuditorAware() {
        return new HeaderAuditorAware();
    }
}
