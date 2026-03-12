package com.edusecure.config;

import com.edusecure.core.tenant.SchoolMultiTenantConnectionProvider;
import com.edusecure.core.tenant.TenantIdentifierResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages            = "com.edusecure",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef   = "transactionManager"
)
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    // On construit HikariCP manuellement avec setJdbcUrl()
    // @ConfigurationProperties ne mappe pas url → jdbcUrl automatiquement
    @Bean
    @Primary
    public DataSource masterDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);                         // ← clé du problème
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName("MasterPool");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("masterDataSource") DataSource dataSource,
            SchoolMultiTenantConnectionProvider connectionProvider,
            TenantIdentifierResolver identifierResolver) {

        LocalContainerEntityManagerFactoryBean em =
                new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(dataSource);
        em.setPackagesToScan("com.edusecure");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> props = new HashMap<>();

        // Dialecte obligatoire en mode multi-tenant
        // Hibernate ne peut pas le détecter automatiquement sans connexion directe
        props.put("hibernate.dialect",
                "org.hibernate.dialect.PostgreSQLDialect");

        props.put("hibernate.multiTenancy",                    "DATABASE");
        props.put("hibernate.multi_tenant_connection_provider", connectionProvider);
        props.put("hibernate.tenant_identifier_resolver",       identifierResolver);
        props.put("hibernate.hbm2ddl.auto",                    "validate");
        props.put("hibernate.format_sql",                      "true");
        props.put("hibernate.show_sql",                        "false");

        em.setJpaPropertyMap(props);
        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(entityManagerFactory.getObject());
        return tm;
    }
}