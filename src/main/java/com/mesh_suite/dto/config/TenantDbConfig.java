package com.mesh_suite.dto.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TenantDbConfig {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String driverClassName;
}
