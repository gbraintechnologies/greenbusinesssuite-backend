package com.mesh_suite.service.company;

import com.mesh_suite.constant.company.BuildStatus;
import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.dto.config.TenantDbConfig;
import com.mesh_suite.interceptor.TenantContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantDatabaseConfigService {

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    private final UserCompanyRepository userCompanyRepository;
    private final Map<String, TenantDbConfig> tenantConfigCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadTenantConfigs() {
        String originalTenant = TenantContext.getCurrentTenant();
        try {
            TenantContext.setCurrentTenant(AppConstants.DEFAULT_TENANT_ID);
            log.debug("Switched to default tenant for loading configs");

            userCompanyRepository.findAll().forEach(company -> {
                String tenantId = company.getCompanyIdentifier();

                // Skip if tenant has no dbUrl (not yet set up)
                if (company.getDbUrl() == null || company.getDbUrl().isEmpty()) {
                    log.warn("Skipping tenant {} - dbUrl not yet configured", tenantId);
                    return;
                }

                // Only load ACTIVE or COMPLETED tenants
                if (company.getBuildStatus() != BuildStatus.ACTIVE &&
                        company.getBuildStatus() != BuildStatus.COMPLETED) {
                    log.warn("Skipping tenant {} - build status: {}", tenantId, company.getBuildStatus());
                    return;
                }

                TenantDbConfig config = createConfigFromCompany(company);
                tenantConfigCache.put(tenantId, config);
                log.info("Loaded config for tenant {}: URL={}, Driver={}",
                        tenantId, config.getJdbcUrl(), config.getDriverClassName());
            });

            log.info("Loaded {} tenant configurations", tenantConfigCache.size());
        } catch (Exception e) {
            log.error("Failed to load tenant configs at startup: {}", e.getMessage(), e);
        } finally {
            TenantContext.setCurrentTenant(originalTenant);
            log.debug("Restored original tenant context: {}", originalTenant);
        }
    }

    public TenantDbConfig getTenantDbConfig(String tenantId) {
        TenantDbConfig config = tenantConfigCache.get(tenantId);

        if (config == null) {
            log.warn("No cached config for tenant {}; dynamically loading from master DB", tenantId);
            String originalTenant = TenantContext.getCurrentTenant();

            try {
                TenantContext.setCurrentTenant(AppConstants.DEFAULT_TENANT_ID);
                log.debug("Switched to default tenant for dynamic config load of {}", tenantId);

                Optional<UserCompany> companyOpt = userCompanyRepository.findByCompanyIdentifier(tenantId);
                if (companyOpt.isEmpty()) {
                    log.error("Tenant config not found in master DB for {}", tenantId);
                    throw new RuntimeException("Tenant config not found for " + tenantId);
                }

                UserCompany company = companyOpt.get();

                // Check if dbUrl is populated
                if (company.getDbUrl() == null || company.getDbUrl().isEmpty()) {
                    log.error("Tenant {} has no dbUrl configured. Build status: {}",
                            tenantId, company.getBuildStatus());
                    throw new RuntimeException("Tenant database URL not configured for " + tenantId);
                }

                // Check if driver name is populated
                if (company.getDriverName() == null || company.getDriverName().isEmpty()) {
                    log.warn("Tenant {} has no driver name, using default: {}", tenantId, driverClassName);
                    company.setDriverName(driverClassName);
                }

                // Check if build status is appropriate
                if (company.getBuildStatus() != BuildStatus.ACTIVE &&
                        company.getBuildStatus() != BuildStatus.COMPLETED) {
                    log.error("Tenant {} build status is {} - database may not be ready",
                            tenantId, company.getBuildStatus());
                    throw new RuntimeException("Tenant database not ready for " + tenantId);
                }

                config = createConfigFromCompany(company);
                tenantConfigCache.put(tenantId, config);
                log.info("Dynamically loaded and cached config for tenant {}: URL={}, Driver={}",
                        tenantId, config.getJdbcUrl(), config.getDriverClassName());

            } catch (Exception e) {
                log.error("Failed to dynamically load config for tenant {}: {}", tenantId, e.getMessage(), e);
                throw new RuntimeException("Failed to load tenant config for " + tenantId, e);
            } finally {
                TenantContext.setCurrentTenant(originalTenant);
                log.debug("Restored original tenant context: {}", originalTenant);
            }
        } else {
            log.debug("Using cached config for tenant {}", tenantId);
        }

        return config;
    }

    private TenantDbConfig createConfigFromCompany(UserCompany company) {
        // Use the driver name from company, fallback to default
        String driverName = company.getDriverName();
        if (driverName == null || driverName.isEmpty()) {
            driverName = driverClassName;
            log.warn("Company {} has no driverName, using default: {}", company.getCompanyIdentifier(), driverName);
        }

        TenantDbConfig config = new TenantDbConfig(
                company.getDbUrl(),
                dbUsername,
                dbPassword,
                driverName
        );

        log.debug("Created config for {} with URL: {}", company.getCompanyIdentifier(), company.getDbUrl());
        return config;
    }

    public Iterable<String> getAllTenantIds() {
        return tenantConfigCache.keySet();
    }

    // Method to refresh cache for a specific tenant
    public void refreshTenantConfig(String tenantId) {
        tenantConfigCache.remove(tenantId);
        log.info("Refreshed cache for tenant: {}", tenantId);
    }
}