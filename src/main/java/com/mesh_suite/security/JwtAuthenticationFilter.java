package com.mesh_suite.security;

import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.interceptor.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtUtil;
    private final MasterTenantValidator masterTenantValidator;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(AppConstants.PUBLIC_PATHS);
    private static final String EXTERNAL_BASE_PATH = "/mesh-suite/v1.0/external/forms-service";
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        try {
            // Check if it's a public endpoint
            boolean isPublic = PUBLIC_ENDPOINTS.stream()
                    .anyMatch(p -> matcher.match(p, path));

            // Also check for actuator endpoints explicitly
            boolean isActuator = path.startsWith("/actuator") || path.startsWith("/health");

            if (isPublic || isActuator) {
                resolvePublicTenant(request, path);
                log.debug("Public endpoint '{}' - tenant set to: {}", path, TenantContext.getCurrentTenant());
                filterChain.doFilter(request, response);
                return;
            }

            // External Client API Endpoints
            if (path.startsWith(EXTERNAL_BASE_PATH)) {
                String token = jwtUtil.resolveToken(request);

                if (!StringUtils.hasText(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: missing API token");
                    return;
                }

                if (!jwtUtil.validateToken(token) || !jwtUtil.isApiToken(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: invalid API token");
                    return;
                }

                String username = jwtUtil.getUsername(token);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_API")
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("External API client authenticated: {}", username);
                filterChain.doFilter(request, response);
                return;
            }

            // Secured routes
            String token = jwtUtil.resolveToken(request);
            if (!StringUtils.hasText(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: missing token");
                return;
            }

            if (!jwtUtil.validateToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: invalid token");
                return;
            }

            String tokenTenant = jwtUtil.getTenantId(token);
            String headerTenant = request.getHeader("tenantid");

            log.debug("Token tenant: {}, Header tenant: {}", tokenTenant, headerTenant);

            boolean isPrivileged = AppConstants.DEFAULT_TENANT_ID.equals(tokenTenant);

            if (isPrivileged && StringUtils.hasText(headerTenant) && !AppConstants.DEFAULT_TENANT_ID.equals(headerTenant)) {
                if (!masterTenantValidator.isValidTenant(headerTenant)) {
                    log.warn("Privileged user attempted to access invalid tenant: {}", headerTenant);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: invalid tenant");
                    return;
                }
                TenantContext.setCurrentTenant(headerTenant);
                log.info("Privileged master user switched to tenant => {}", headerTenant);
            } else {
                String finalTenant = StringUtils.hasText(tokenTenant) ? tokenTenant : AppConstants.DEFAULT_TENANT_ID;
                TenantContext.setCurrentTenant(finalTenant);
                log.debug("Using tenant from token: {}", finalTenant);
            }

            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null && !AppConstants.DEFAULT_TENANT_ID.equals(currentTenant)) {
                if (!masterTenantValidator.isValidTenant(currentTenant)) {
                    log.warn("Invalid tenant after validation: {}", currentTenant);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: invalid tenant");
                    return;
                }
            }

            Authentication auth;
            if (isPrivileged) {
                String username = jwtUtil.getUsername(token);
                String roles = jwtUtil.extractClaim(token, claims -> claims.get("roles", String.class));
                auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(roles)
                );
                log.debug("Master privileged user authenticated: {}", username);
            } else {
                auth = jwtUtil.getAuthentication(token);
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in JWT authentication filter: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error");
        } finally {
            // Don't clear context for public endpoints
            if (!path.startsWith("/actuator") && !path.startsWith("/health")) {
                // Clear context only for non-public endpoints
                // But we need to be careful - better to clear in all cases
                // SecurityContextHolder.clearContext();
            }
        }
    }

    private void resolvePublicTenant(HttpServletRequest request, String path) {
        String tenantId = request.getParameter(AppConstants.TENANT_ID_PARAM);
        if (!StringUtils.hasText(tenantId)) {
            tenantId = request.getHeader(AppConstants.TENANT_ID_PARAM);
        }
        String finalTenant = StringUtils.hasText(tenantId) ? tenantId : AppConstants.DEFAULT_TENANT_ID;
        TenantContext.setCurrentTenant(finalTenant);
        log.debug("Public endpoint '{}', tenant set to: {}", path, finalTenant);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}