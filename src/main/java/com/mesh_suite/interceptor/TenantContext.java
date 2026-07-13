package com.mesh_suite.interceptor;

import com.mesh_suite.constant.shared.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            CURRENT_TENANT.set(AppConstants.DEFAULT_TENANT_ID);
            log.trace("Tenant context set to default: {}", AppConstants.DEFAULT_TENANT_ID);
            return;
        }
        CURRENT_TENANT.set(tenantId.trim());
        log.trace("Tenant context set to: {}", tenantId);
    }

    public static String getCurrentTenant() {
        return Optional.ofNullable(CURRENT_TENANT.get()).orElse(AppConstants.DEFAULT_TENANT_ID);
    }

    public static void clear() {
        String current = CURRENT_TENANT.get();
        CURRENT_TENANT.remove();
        log.trace("Tenant context cleared (was: {})", current);
    }

    public static boolean isTenantSet() {
        return CURRENT_TENANT.get() != null;
    }
}
