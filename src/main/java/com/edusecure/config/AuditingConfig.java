package com.edusecure.config;

import com.edusecure.core.tenant.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            // Pas d'utilisateur connecté (ex: appel système)
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("system");
            }

            // Format : "tenantId:username" pour traçabilité complète
            String tenant = TenantContext.getCurrentTenant();
            String user   = auth.getName();

            return Optional.of(
                    tenant != null ? tenant + ":" + user : user
            );
        };
    }
}