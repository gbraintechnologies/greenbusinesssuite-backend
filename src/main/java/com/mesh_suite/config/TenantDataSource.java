package com.mesh_suite.config;

import com.mesh_suite.dto.config.TenantDbConfig;
import com.mesh_suite.dto.config.TenantPool;

import com.mesh_suite.service.company.TenantDatabaseConfigService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class TenantDataSource {

    private static final int MAX_CACHED_TENANTS = 50;
    private static final long IDLE_LIMIT = 30 * 60 * 1000; // 30 mins

    private final Map<String, TenantPool> pools = Collections.synchronizedMap(
            new LinkedHashMap<String, TenantPool>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, TenantPool> eldest) {
                    if (size() > MAX_CACHED_TENANTS) {
                        TenantPool pool = eldest.getValue();
                        long idle = System.currentTimeMillis() - pool.getLastUsed();
                        HikariDataSource ds = pool.getDataSource();
                        if (idle > IDLE_LIMIT && ds.getHikariPoolMXBean().getActiveConnections() == 0) {
                            log.warn("Evicting idle eldest tenant pool: {}", eldest.getKey());
                            ds.close();
                            return true;
                        }
                    }
                    return false;
                }
            }
    );

    private final TenantDatabaseConfigService tenantConfigService;

    public HikariDataSource getTenantDataSource(String tenantId) {
        TenantPool pool = pools.get(tenantId);

        if (pool == null || pool.getDataSource().isClosed()) {
            return createAndCacheTenantPool(tenantId);
        }

        pool.touch();
        return pool.getDataSource();
    }

    private synchronized HikariDataSource createAndCacheTenantPool(String tenantId) {
        TenantPool existing = pools.get(tenantId);
        if (existing != null && !existing.getDataSource().isClosed()) {
            existing.touch();
            return existing.getDataSource();
        }

        log.info("Creating new connection pool for tenant: {}", tenantId);

        try {
            TenantDbConfig config = tenantConfigService.getTenantDbConfig(tenantId);
            log.info("Tenant {} config → URL: {}, Username: {}, Driver: {}",
                    tenantId, config.getJdbcUrl(), config.getUsername(), config.getDriverClassName());

            HikariConfig hikari = new HikariConfig();
            hikari.setJdbcUrl(config.getJdbcUrl());
            hikari.setUsername(config.getUsername());
            hikari.setPassword(config.getPassword());
            hikari.setDriverClassName(config.getDriverClassName());
            hikari.setPoolName("TenantPool-" + tenantId);
            hikari.setMaximumPoolSize(10);
            hikari.setMinimumIdle(1);
            hikari.setConnectionTimeout(30_000);
            hikari.setValidationTimeout(5000);
            hikari.setConnectionTestQuery("SELECT 1");

            HikariDataSource ds = new HikariDataSource(hikari);

            // Immediate connection test with retry
            boolean connected = false;
            int maxRetries = 3;
            for (int i = 0; i < maxRetries && !connected; i++) {
                try (Connection conn = ds.getConnection()) {
                    log.info("SUCCESS: Validated connection to tenant {} database (attempt {})", tenantId, i + 1);
                    connected = true;
                } catch (Exception e) {
                    log.warn("Connection attempt {} failed for tenant {}: {}", i + 1, tenantId, e.getMessage());
                    if (i == maxRetries - 1) {
                        throw e;
                    }
                    Thread.sleep(2000); // Wait before retry
                }
            }

            pools.put(tenantId, new TenantPool(ds));
            return ds;

        } catch (Exception e) {
            log.error("Failed to create pool for tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Failed to create datasource for tenant: " + tenantId, e);
        }
    }

    @PostConstruct
    private void startEvictionThread() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    evictIdlePools();
                    Thread.sleep(5 * 60 * 1000);
                } catch (Exception e) {
                    log.error("Tenant pool eviction error", e);
                }
            }
        }, "TenantPool-Evictor");
        t.setDaemon(true);
        t.start();
    }

    private void evictIdlePools() {
        long now = System.currentTimeMillis();
        synchronized (pools) {
            pools.entrySet().removeIf(entry -> {
                TenantPool pool = entry.getValue();
                HikariDataSource ds = pool.getDataSource();
                if (ds.isClosed()) return true;
                long idle = now - pool.getLastUsed();
                int active = ds.getHikariPoolMXBean().getActiveConnections();
                if (active == 0 && idle > IDLE_LIMIT) {
                    log.info("Closing idle tenant pool: {}", entry.getKey());
                    ds.close();
                    return true;
                }
                return false;
            });
        }
    }
}