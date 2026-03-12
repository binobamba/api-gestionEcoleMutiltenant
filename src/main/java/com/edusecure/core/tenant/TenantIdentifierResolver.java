package com.edusecure.core.tenant;


import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class TenantIdentifierResolver  implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getCurrentTenant();

        if (tenant != null) {
            return tenant;
        }

        // Pas de tenant dans le contexte → master DB
        log.debug("Aucun tenant dans le contexte, utilisation de : {}",
                DEFAULT_TENANT);
        return DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    // Enregistre ce resolver dans Hibernate via les properties JPA

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(
                AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

}
