package com.edusecure.core.tenant;

import com.edusecure.config.properties.TenantProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Component
@Order(1) // Premier filtre — avant Spring Security
@RequiredArgsConstructor
public class TenantFilter implements Filter {

    private final TenantProperties tenantProperties;

    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        try {
            String tenantId = resolveTenant(request);

            if (StringUtils.hasText(tenantId)) {
                // 1. Stocke dans le ThreadLocal
                TenantContext.setCurrentTenant(tenantId);

                // 2. Apparaît dans tous les logs de cette requête
                MDC.put("tenantId", tenantId);

                // 3. Renvoie dans la réponse pour debug
                response.setHeader("X-Tenant-ID", tenantId);

                log.debug("Tenant résolu : {}", tenantId);
            }

            chain.doFilter(request, response);

        } finally {
            // TOUJOURS nettoyer même si exception
            TenantContext.clear();
            MDC.remove("tenantId");
        }
    }

    private String resolveTenant(HttpServletRequest request) {

        // Priorité 1 : header X-Tenant-ID
        String header = request.getHeader(tenantProperties.getHeaderName());
        if (StringUtils.hasText(header)) {
            return header.toLowerCase().trim();
        }

        // Priorité 2 : sous-domaine
        // lycee-hugo.monapi.com → lycee-hugo
        String host = request.getServerName();
        if (host != null && host.contains(".")) {
            String subdomain = host.split("\\.")[0];
            if (!subdomain.equals("www") && !subdomain.equals("api")) {
                return subdomain.toLowerCase();
            }
        }

        return null;
    }
}