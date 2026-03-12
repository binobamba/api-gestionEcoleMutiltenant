package com.edusecure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.tenant")
public class TenantProperties {
    private String headerName = "X-Tenant-ID";
    private String dbPrefix   = "school_tenant_";
    private String dbHost     = "localhost";
    private String dbPort     = "5432";
    private String dbUser     = "postgres";
    private String dbPassword = "postgres";
}