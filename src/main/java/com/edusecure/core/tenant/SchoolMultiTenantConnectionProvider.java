package com.edusecure.core.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
public class SchoolMultiTenantConnectionProvider
        implements org.hibernate.engine.jdbc.connections.spi
        .MultiTenantConnectionProvider<String> {

    private final TenantDataSourceManager dataSourceManager;

    // @Lazy : Spring injecte le proxy d'abord, résout le bean après
    // Casse la référence circulaire
    private final DataSource masterDataSource;

    @Autowired
    public SchoolMultiTenantConnectionProvider(
            TenantDataSourceManager dataSourceManager,
            @Lazy DataSource masterDataSource) {
        this.dataSourceManager = dataSourceManager;
        this.masterDataSource  = masterDataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return masterDataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantId) throws SQLException {
        log.debug("Connexion pour tenant : {}", tenantId);
        if ("public".equals(tenantId)) {
            return masterDataSource.getConnection();
        }
        return dataSourceManager.getDataSource(tenantId).getConnection();
    }

    @Override
    public void releaseConnection(String tenantId,
                                  Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() { return false; }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) { return false; }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException();
    }
}