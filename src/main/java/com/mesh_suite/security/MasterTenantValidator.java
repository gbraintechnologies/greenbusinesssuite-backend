package com.mesh_suite.security;

import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.interceptor.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class MasterTenantValidator {

    private final UserCompanyRepository userCompanyRepository;

    public boolean isValidTenant(String tenantId) {
        // If tenant is blank, treat it as default
        if (!StringUtils.hasText(tenantId)) {
            log.debug("Blank tenantId → using default tenant '{}'", AppConstants.DEFAULT_TENANT_ID);
            return true;
        }

        // Default tenant is always valid (fast path)
        if (AppConstants.DEFAULT_TENANT_ID.equalsIgnoreCase(tenantId)) {
            log.debug("Tenant '{}' accepted (default tenant)", tenantId);
            return true;
        }

        // Validate external tenants using master DB
        String originalTenant = TenantContext.getCurrentTenant();
        try {
            TenantContext.setCurrentTenant(AppConstants.DEFAULT_TENANT_ID);

            boolean exists = userCompanyRepository
                    .findByCompanyIdentifier(tenantId)
                    .isPresent();

            log.debug("Validation for tenant '{}': {}", tenantId, exists);
            return exists;

        } catch (Exception e) {
            log.error("Error validating tenant '{}': {}", tenantId, e.getMessage());
            return false;
        } finally {
            TenantContext.setCurrentTenant(originalTenant);
        }
    }
}