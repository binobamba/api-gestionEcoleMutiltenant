package com.edusecure.core.tenant;

import com.edusecure.config.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final TenantProperties        tenantProperties;
    private final TenantDataSourceManager dataSourceManager;

    @Value("${spring.datasource.url}")
    private String masterDbUrl;

    @Value("${spring.datasource.username}")
    private String masterDbUser;

    @Value("${spring.datasource.password}")
    private String masterDbPassword;

    /**
     * Provisionne un nouveau tenant :
     * 1. Crée la base de données PostgreSQL
     * 2. Exécute les migrations Flyway
     * 3. Enregistre le DataSource dans le cache
     */

    public void provisionTenant(String tenantId) {
        validateTenantId(tenantId);

        String dbName = tenantProperties.getDbPrefix() + tenantId;
        String dbUrl  = buildTenantUrl(dbName);

        log.info("Provisionnement tenant : {} → BDD : {}", tenantId, dbName);

        // Étape 1 : créer la BDD
        createDatabase(dbName);

        // Étape 2 : migrer le schéma avec Flyway
        runMigrations(dbUrl);

        // Étape 3 : enregistrer le DataSource
        dataSourceManager.registerDataSource(
                tenantId,
                dbUrl,
                masterDbUser,
                masterDbPassword
        );

        log.info("Tenant provisionné avec succès : {}", tenantId);
    }

    /**
     * Supprime le DataSource du cache.
     * Les données restent en base — soft suspension.
     */

    public void suspendTenant(String tenantId) {
        dataSourceManager.evict(tenantId);
        log.info("Tenant suspendu : {}", tenantId);
    }

    // Crée la base de données sur le serveur PostgreSQL
    private void createDatabase(String dbName) {
        // On se connecte à "postgres" (BDD système) pour créer une nouvelle BDD
        String serverUrl = masterDbUrl.substring(
                0, masterDbUrl.lastIndexOf('/') + 1) + "postgres";

        try (Connection conn = DriverManager.getConnection(
                serverUrl, masterDbUser, masterDbPassword);
             Statement stmt = conn.createStatement()) {
            // Vérifie si la BDD existe déjà
            var rs = conn.getMetaData().getCatalogs();
            while (rs.next()) {
                if (dbName.equals(rs.getString(1))) {
                    log.warn("BDD déjà existante : {}", dbName);
                    return;
                }
            }
            stmt.execute("CREATE DATABASE \"" + dbName + "\"");
            log.info("BDD créée : {}", dbName);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur création BDD : " + dbName, e);
        }
    }

    // Exécute les migrations Flyway sur la BDD du tenant
    private void runMigrations(String dbUrl) {
        log.info("Migration Flyway sur : {}", dbUrl);
        Flyway flyway = Flyway.configure()
                .dataSource(dbUrl, masterDbUser, masterDbPassword)
                .locations("classpath:db/tenant-migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
        var result = flyway.migrate();
        log.info("Migrations appliquées : {}", result.migrationsExecuted);
    }

    private String buildTenantUrl(String dbName) {
        String base = masterDbUrl.substring(
                0, masterDbUrl.lastIndexOf('/') + 1);
        return base + dbName;
    }

    // tenantId : 3-50 caractères, minuscules, chiffres, tirets, underscores
    private void validateTenantId(String tenantId) {
        if (!tenantId.matches("^[a-z0-9_-]{3,50}$")) {
            throw new IllegalArgumentException(
                    "TenantId invalide : doit être 3-50 caractères, " +
                            "minuscules, chiffres, tirets ou underscores. Reçu : "
                            + tenantId);
        }
    }
}
