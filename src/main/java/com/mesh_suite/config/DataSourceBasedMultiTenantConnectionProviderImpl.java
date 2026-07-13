package com.mesh_suite.config;

import com.mesh_suite.constant.shared.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataSourceBasedMultiTenantConnectionProviderImpl
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {


    private final DataSource defaultDataSource;
    private final TenantDataSource tenantDataSource;

    @Override
    protected DataSource selectAnyDataSource() {
        return defaultDataSource;
    }

    @Override
    protected DataSource selectDataSource(Object tenantIdentifier) {

        String tenantId = tenantIdentifier == null ? null : tenantIdentifier.toString();

        if (tenantId == null || tenantId.equals(AppConstants.DEFAULT_TENANT_ID)) {
            return defaultDataSource;
        }

        try {
            return tenantDataSource.getTenantDataSource(tenantId);
        } catch (Exception e) {
            log.error("Failed to get tenant datasource for {}: {}", tenantId, e.getMessage());
            return defaultDataSource; // fail-safe
        }
    }
}

