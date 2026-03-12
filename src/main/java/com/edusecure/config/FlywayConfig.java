package com.edusecure.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class FlywayConfig {

    // Flyway pour la master DB — s'exécute au démarrage
    @Bean(initMethod = "migrate")
    public Flyway masterFlyway(
            @Qualifier("masterDataSource") DataSource masterDataSource) {

        log.info("Exécution des migrations Flyway sur la master DB...");

        return Flyway.configure()
                .dataSource(masterDataSource)
                .locations("classpath:db/migration")
                .ignoreMigrationPatterns("*:missing")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }
}