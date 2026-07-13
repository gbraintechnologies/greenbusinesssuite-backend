package com.mesh_suite.interceptor;

import com.mesh_suite.constant.shared.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class RequestInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // Only set tenant if not already set by JwtAuthenticationFilter
        if (!TenantContext.isTenantSet()) {
            String tenantId = request.getHeader("tenantid");

            if (StringUtils.hasText(tenantId)) {
                TenantContext.setCurrentTenant(tenantId);
                log.debug("RequestInterceptor: Tenant ID set from header: {}", tenantId);
            } else {
                TenantContext.setCurrentTenant(AppConstants.DEFAULT_TENANT_ID);
                log.debug("RequestInterceptor: No Tenant ID in header. Using default.");
            }
        } else {
            log.trace("RequestInterceptor: Tenant already set by filter, skipping.");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //  TenantContext.clear() - Now handled only in JwtAuthenticationFilter

        log.trace("RequestInterceptor: Request completed for path: {}", request.getRequestURI());
    }
}
