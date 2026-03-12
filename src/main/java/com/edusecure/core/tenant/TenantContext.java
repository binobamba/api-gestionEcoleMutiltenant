package com.edusecure.core.tenant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TenantContext {

    /**
     * InheritableThreadLocal : les threads enfants héritent
     * de la valeur du parent (important pour @Async)
     */

    private static final ThreadLocal<String> CURRENT_TENANT = new InheritableThreadLocal<>();

    // Classe utilitaire — pas d'instanciation

    private TenantContext() {}

    public static void setCurrentTenant(String tenantId) {
        log.debug("Tenant activé : {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    // TOUJOURS appeler dans un bloc finally — évite les memory leaks

    public static void clear() {
        log.debug("Tenant effacé");
        CURRENT_TENANT.remove();
    }

    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}