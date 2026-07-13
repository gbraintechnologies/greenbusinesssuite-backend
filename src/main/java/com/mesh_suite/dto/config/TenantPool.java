package com.mesh_suite.dto.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

@Getter
public class TenantPool {

    private final HikariDataSource dataSource;
    private volatile long lastUsed;

    public TenantPool(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        this.lastUsed = System.currentTimeMillis();
    }

    public void touch() {

        lastUsed = System.currentTimeMillis();
    }
}
