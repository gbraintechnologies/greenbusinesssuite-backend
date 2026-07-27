package com.mesh_suite.constant.shared;

public final class AppConstants {

    private AppConstants() {}

    public static final String[] PUBLIC_PATHS = {
            "/mesh-suite/v1.0/auth/**",
            "/mesh-suite/v1.0/forms/builder/**",
            "/mesh-suite/v1.0/categories",
            "/mesh-suite/v1.0/company-branding/**",
            "/mesh-suite/v1.0/s3/resource/**",
            "/mesh-suite/v1.0/media/**",
            "/mesh-suite/v1.0/external/forms-service/client",
            "/mesh-suite/v1.0/external/forms-service/token",
            "/mesh-suite/v1.0/external/forms-service/forms/create",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/**",
            "/health",
            "/health/**",
            "/",
            "/api/health"
    };

    public static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3000",
            "https://staging.meshsuites.com",
            "https://admin.meshsuites.com",
            "https://staging.thefaithhq.com",
            "https://thefaithhq.com",
            "https://entityrail.com",
            "https://www.entityrail.com",
            "https://api.entityrail.com",
            "https://greenbusinesssuite.com",
            "https://www.greenbusinesssuite.com",
            "https://api.greenbusinesssuite.com"
    };

    public static final String DEFAULT_TENANT_ID = "mesh_suite_db";
    public static final String ENTITY_PACKAGE_SCAN = "com.mesh_suite.domain";
    public static final String CALLBACK_PATH = "/mesh-suite/v1.0/payments/trigger/callback";
    public static final String TENANT_ID_PARAM = "tenantid";
    public static final String CHECK_TRANSACTION_URL = "https://orchard-api.anmgw.com/checkTransaction";
}