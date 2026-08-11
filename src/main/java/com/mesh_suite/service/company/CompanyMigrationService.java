package com.mesh_suite.service.company;

import com.mesh_suite.constant.company.BuildStatus;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.domain.user.Role;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyMigrationService {

    @Value("${spring.datasource.url}")
    private String masterDbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final UserCompanyRepository userCompanyRepository;
    private final TenantDatabaseConfigService tenantConfigService;

    @Async
    public void provisionTenantDatabase(Long companyId) {
        UserCompany company = userCompanyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        String tenantId = company.getCompanyIdentifier();

        try {
            log.info("Provisioning tenant database: {}", tenantId);

            // Step 1: Create the physical DB
            createDatabase(tenantId);

            // Step 2: Bootstrap schema + seed using a direct DataSource
            //         — bypasses TenantDataSource entirely, no status check needed
            DataSource tenantDs = buildDirectDataSource(company.getDbUrl());
            bootstrapSchema(tenantDs, tenantId);

            // Step 3: Mark ACTIVE and warm up the tenant pool for live traffic
            company.setBuildStatus(BuildStatus.ACTIVE);
            userCompanyRepository.save(company);

            // Step 4: Now it's safe to warm up — status is ACTIVE, config check will pass
            tenantConfigService.refreshTenantConfig(tenantId);

            log.info("Tenant provisioned successfully: {}", tenantId);

        } catch (Exception e) {
            log.error("Tenant provisioning failed: {}", tenantId, e);
            userCompanyRepository.findById(companyId).ifPresent(failedCompany -> {
                failedCompany.setBuildStatus(BuildStatus.FAILED);
                userCompanyRepository.save(failedCompany);
            });
        }
    }

    // ── Step 1 ────────────────────────────────────────────────────────────────

    private void createDatabase(String dbName) {
        String adminUrl = masterDbUrl.replaceAll("/[^/]+$", "/postgres");
        try (Connection conn = DriverManager.getConnection(adminUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
            log.info("Database created: {}", dbName);
        } catch (SQLException e) {
            if ("42P04".equals(e.getSQLState())) {
                log.warn("Database already exists, continuing: {}", dbName);
            } else {
                throw new RuntimeException("Failed to create database: " + dbName, e);
            }
        }
    }

    // ── Step 2: Direct DataSource — no status gate, no cache ─────────────────

    private DataSource buildDirectDataSource(String jdbcUrl) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPassword);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(3);
        config.setConnectionTimeout(30_000);
        return new HikariDataSource(config);
    }

    private void bootstrapSchema(DataSource tenantDs, String tenantId) {
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(tenantDs);
        emfBean.setPackagesToScan("com.mesh_suite.domain");
        emfBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emfBean.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"
        ));
        emfBean.afterPropertiesSet();

        EntityManagerFactory emf = Objects.requireNonNull(emfBean.getObject());
        try {
            seedDefaultRoles(emf);
            log.info("Schema bootstrapped for tenant: {}", tenantId);
        } finally {
            emf.close();
        }
    }

    private void seedDefaultRoles(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(Role.builder().roleName("ADMIN").description("Full access").createdAt(LocalDateTime.now()).build());
            em.persist(Role.builder().roleName("CLIENT").description("Limited access").createdAt(LocalDateTime.now()).build());
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to seed default roles", e);
        } finally {
            em.close();
        }
    }
}