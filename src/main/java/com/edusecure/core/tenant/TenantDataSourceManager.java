package com.edusecure.core.tenant;

import com.edusecure.config.properties.TenantProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantDataSourceManager {

    private final TenantProperties tenantProperties;

    // Cache thread-safe : tenantId → DataSource
    private final Map<String, HikariDataSource> cache =
            new ConcurrentHashMap<>();

    /**
     * Retourne le DataSource du tenant.
     * Le crée s'il n'existe pas encore dans le cache.
     * computeIfAbsent est atomique → thread-safe.
     */

    public DataSource getDataSource(String tenantId) {
        return cache.computeIfAbsent(tenantId, this::createDataSource);
    }

    /**
     * Enregistre un DataSource manuellement.
     * Appelé par TenantProvisioningService après création d'une nouvelle BDD.
     */

    public DataSource registerDataSource(String tenantId, String url, String username,String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName("Pool-" + tenantId);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);

        HikariDataSource ds = new HikariDataSource(config);
        cache.put(tenantId, ds);
        log.info("DataSource enregistré pour tenant : {}", tenantId);
        return ds;
    }

    /**
     * Expulse le DataSource du cache.
     * Appelé après suspension ou suppression d'un tenant.
     */

    public void evict(String tenantId) {
        HikariDataSource ds = cache.remove(tenantId);
        if (ds != null && !ds.isClosed()) {
            ds.close();
            log.info("DataSource fermé pour tenant : {}", tenantId);
        }
    }

    // Crée un DataSource HikariCP à partir des properties
    private HikariDataSource createDataSource(String tenantId) {
        String dbName = tenantProperties.getDbPrefix() + tenantId;
        String url    = "jdbc:postgresql://"
                + tenantProperties.getDbHost() + ":"
                + tenantProperties.getDbPort() + "/"
                + dbName;

        log.info("Création DataSource → tenant: {} | BDD: {}", tenantId, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(tenantProperties.getDbUser());
        config.setPassword(tenantProperties.getDbPassword());
        config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName("Pool-" + tenantId);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);

        // Optimisations PostgreSQL
        config.addDataSourceProperty("cachePrepStmts",    "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");

        return new HikariDataSource(config);
    }

    // Ferme proprement tous les pools à l'arrêt de l'application
    @PreDestroy
    public void closeAll() {
        log.info("Fermeture de tous les DataSources tenant...");
        cache.values().forEach(ds -> {
            if (!ds.isClosed()) ds.close();
        });
        cache.clear();
    }
}