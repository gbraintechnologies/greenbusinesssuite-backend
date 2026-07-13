package com.mesh_suite.config;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.constant.forms.CompanyCurrency;
import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.dao.user.PermissionRepository;
import com.mesh_suite.dao.user.RoleRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.domain.user.Permission;
import com.mesh_suite.domain.user.Role;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.interceptor.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserCompanyRepository userCompanyRepository;

    private static final List<PermissionSeed> PERMISSIONS = Arrays.asList(
            new PermissionSeed("company", "branch", "search", "Search Company Branch"),
            new PermissionSeed("jurisdictions", null, "create", "Create Jurisdictions"),
            new PermissionSeed("jurisdictions", null, "read_all", "Read All Jurisdictions"),
            new PermissionSeed("jurisdictions", null, "read", "Read Jurisdiction"),
            new PermissionSeed("jurisdictions", null, "edit", "Edit Jurisdiction"),
            new PermissionSeed("jurisdictions", null, "search", "Search Jurisdiction"),
            new PermissionSeed("currency", null, "create", "Create Currency"),
            new PermissionSeed("currencies", null, "read_all", "Read All Currencies"),
            new PermissionSeed("currency", null, "read", "Read Currency"),
            new PermissionSeed("currency", null, "edit", "Edit Currency"),
            new PermissionSeed("currency", "denomination", "create", "Create Currency Denomination"),
            new PermissionSeed("currency", "denomination", "read_all", "Read All Currency Denomination"),
            new PermissionSeed("currency", "denomination", "read", "Read Currency Denomination"),
            new PermissionSeed("company", "assign", "create", "Create Assign Company"),
            new PermissionSeed("company", "assign", "read", "Read Assigned Company"),
            new PermissionSeed("company", null, "read", "Read Company"),
            new PermissionSeed("permission", null, "create", "Create Permission"),
            new PermissionSeed("permission", null, "read", "Read Permission"),
            new PermissionSeed("permission", "assign", "assign", "Assign Permission"),
            new PermissionSeed("permission", null, "delete", "Delete Permission"),
            new PermissionSeed("role", null, "create", "Create Role"),
            new PermissionSeed("role", null, "update", "Update Role"),
            new PermissionSeed("role", null, "read", "Read Role"),
            new PermissionSeed("user", null, "read_all", "Read All Users"),
            new PermissionSeed("user", null, "read", "Read User"),
            new PermissionSeed("user", null, "edit", "Edit User"),
            new PermissionSeed("user", null, "create", "Create User"),
            new PermissionSeed("user", null, "blacklist", "Blacklist User"),
            new PermissionSeed("user", "search", "read", "Read User Search"),
            new PermissionSeed("custom_field", null, "create", "Create Custom Fields"),
            new PermissionSeed("custom_field", null, "edit", "Edit Custom Fields"),
            new PermissionSeed("user_profile", null, "create", "Create User Profile"),
            new PermissionSeed("custom_field", null, "read", "Read Custom Fields"),
            new PermissionSeed("user_kyc", null, "read_all", "Read All User KYC"),
            new PermissionSeed("user_kyc", null, "read", "Read User KYC"),
            new PermissionSeed("kyc", null, "create", "Create KYC"),
            new PermissionSeed("user_apps", null, "read_all", "Read All User Apps"),
            new PermissionSeed("user_geo_levels", null, "read", "Read User Geo Levels"),
            new PermissionSeed("user_geo_levels", null, "create", "Create User Geo Levels"),
            new PermissionSeed("users", null, "edit_all", "Edit All Users"),
            new PermissionSeed("users_kyc", null, "read_all", "Read All Users KYC"),
            new PermissionSeed("users_apps", null, "read_all", "Read All Users Apps"),
            new PermissionSeed("users_geo_levels", null, "read_all", "Read All Users Geo Levels"),
            new PermissionSeed("address_scheme", null, "create", "Create Address Scheme"),
            new PermissionSeed("address_scheme", null, "edit", "Edit Address Scheme"),
            new PermissionSeed("address_scheme", null, "read", "Read Address Scheme"),
            new PermissionSeed("address_scheme", null, "read_all", "Read All Address Scheme"),
            new PermissionSeed("address_level", null, "create", "Create Address Level"),
            new PermissionSeed("address_level", null, "read", "Read Address Level"),
            new PermissionSeed("address_level", null, "read_all", "Read All Address Level"),
            new PermissionSeed("address_level", null, "edit", "Edit Address Level"),
            new PermissionSeed("company", null, "create", "Create Company"),
            new PermissionSeed("companies", null, "read_all", "Read All Companies"),
            new PermissionSeed("company", null, "edit", "Edit Company"),
            new PermissionSeed("company", null, "delete", "Delete Company"),
            new PermissionSeed("company", null, "search", "Search Company"),
            new PermissionSeed("company_custom_field", null, "create", "Create Company Custom Field"),
            new PermissionSeed("company_custom_field", null, "read", "Read Company Custom Field"),
            new PermissionSeed("company_branch", null, "create", "Create Company Branch"),
            new PermissionSeed("company_branch", null, "read", "Read Company Branch"),
            new PermissionSeed("company_branches", null, "read", "Read Company Branches"),
            new PermissionSeed("company_branch", null, "edit", "Edit Company Branch"),
            new PermissionSeed("company_branch", null, "delete", "Delete Company Branch")
    );

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========================================");
        log.info("🚀 Starting data initialization...");
        log.info("========================================");

        try {
            initializeDefaultTenant();
            initializePermissions();
            initializeRolesWithPermissions();
            initializeApexUser();
            ensureApexHasAllPermissions();

            log.info("========================================");
            log.info("✅ Data initialization completed successfully!");
            log.info("========================================");

        } catch (Exception e) {
            log.error("❌ Data initialization failed: {}", e.getMessage(), e);
            throw new RuntimeException("Data initialization failed", e);
        }
    }

    // ==================== PRIVATE METHODS ====================

    @Transactional
    protected void initializeDefaultTenant() {
        log.info("📌 Creating default tenant...");

        try {
            TenantContext.setCurrentTenant(AppConstants.DEFAULT_TENANT_ID);

            boolean exists = userCompanyRepository
                    .findByCompanyIdentifier(AppConstants.DEFAULT_TENANT_ID)
                    .isPresent();

            if (!exists) {
                UserCompany defaultTenant = UserCompany.builder()
                        .companyName("Mesh Suite Master Tenant")
                        .companyIdentifier(AppConstants.DEFAULT_TENANT_ID)
                        .primaryContactName("System Administrator")
                        .primaryContactEmail("system@mesh-suite.com")
                        .primaryContactPhoneNumber("+233000000000")
                        .companyAddress("Accra, Ghana")
                        .status(CompanyStatus.ACTIVE)
                        .primaryCurrency(CompanyCurrency.GHC)
                        .companyCode("MESH001")
                        .createdOn(ZonedDateTime.now())
                        .isDeleted(false)
                        .build();

                userCompanyRepository.save(defaultTenant);
                log.info("✅ Default tenant created: {}", AppConstants.DEFAULT_TENANT_ID);
            } else {
                log.info("✅ Default tenant already exists");
            }

        } catch (Exception e) {
            log.error("❌ Failed to create default tenant: {}", e.getMessage(), e);
            throw new RuntimeException("Default tenant creation failed", e);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    protected void initializePermissions() {
        log.info("📌 Creating permissions...");

        try {
            int createdCount = 0;
            for (PermissionSeed perm : PERMISSIONS) {
                String permissionName = Permission.buildName(perm.module(), perm.subModule(), perm.action());
                if (permissionRepository.findByName(permissionName).isEmpty()) {
                    permissionRepository.save(Permission.builder()
                            .name(permissionName)
                            .module(perm.module())
                            .subModule(perm.subModule())
                            .action(perm.action())
                            .description(perm.description())
                            .build());
                    createdCount++;
                }
            }
            log.info("✅ Created {} new permissions", createdCount);

        } catch (Exception e) {
            log.error("❌ Failed to initialize permissions: {}", e.getMessage(), e);
            throw new RuntimeException("Permission initialization failed", e);
        }
    }

    @Transactional
    protected void initializeRolesWithPermissions() {
        log.info("📌 Creating roles with permissions...");

        try {
            List<String> roleNames = Arrays.asList("APEX", "SUPERADMIN", "ADMIN");
            List<Permission> allPermissions = permissionRepository.findAll();

            int createdCount = 0;
            for (String roleName : roleNames) {
                if (roleRepository.findByRoleName(roleName).isPresent()) {
                    log.info("Role '{}' already exists, skipping", roleName);
                    continue;
                }

                Role role = Role.builder()
                        .roleName(roleName)
                        .description(getRoleDescription(roleName))
                        .createdAt(LocalDateTime.now())
                        .permissions(new HashSet<>(allPermissions))
                        .build();

                roleRepository.save(role);
                createdCount++;
                log.info("✅ Created role: {} with {} permissions", roleName, allPermissions.size());
            }

            log.info("✅ Created {} new roles", createdCount);

        } catch (Exception e) {
            log.error("❌ Failed to initialize roles: {}", e.getMessage(), e);
            throw new RuntimeException("Role initialization failed", e);
        }
    }

    @Transactional
    protected void initializeApexUser() {
        log.info("📌 Creating APEX user...");

        String apexEmail = "apex@mesh-suite.logiciel";

        try {
            TenantContext.setCurrentTenant(AppConstants.DEFAULT_TENANT_ID);

            if (!userRepository.existsByEmail(apexEmail)) {
                Role apexRole = roleRepository.findByRoleName("APEX")
                        .orElseThrow(() -> new RuntimeException("APEX role not found"));

                Users apexUser = Users.builder()
                        .username("apex_user")
                        .email(apexEmail)
                        .password(passwordEncoder.encode("passwordApex@123"))
                        .firstName("Apex")
                        .lastName("User")
                        .phoneNumber("+1234567890")
                        .role(apexRole)
                        .status(UserStatus.ACTIVE)
                        .roleName("APEX")
                        .createdOn(LocalDateTime.now())
                        .companyIdentifier(AppConstants.DEFAULT_TENANT_ID)
                        .profileImage("")
                        .isVerified(true)
                        .build();

                userRepository.save(apexUser);
                log.info("✅ APEX user created: {} / password: passwordApex@123", apexEmail);
            } else {
                log.info("✅ APEX user already exists: {}", apexEmail);
            }

        } catch (Exception e) {
            log.error("❌ Failed to create APEX user: {}", e.getMessage(), e);
            throw new RuntimeException("APEX user creation failed", e);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    protected void ensureApexHasAllPermissions() {
        log.info("📌 Ensuring APEX role has all permissions...");

        try {
            Role apexRole = roleRepository.findByRoleName("APEX")
                    .orElseThrow(() -> new RuntimeException("APEX role not found"));

            List<Permission> allPermissions = permissionRepository.findAll();
            Set<Permission> currentPermissions = apexRole.getPermissions();

            int addedCount = 0;
            for (Permission permission : allPermissions) {
                if (!currentPermissions.contains(permission)) {
                    currentPermissions.add(permission);
                    addedCount++;
                }
            }

            if (addedCount > 0) {
                roleRepository.save(apexRole);
                log.info("✅ Added {} missing permissions to APEX role", addedCount);
            } else {
                log.info("✅ APEX role already has all {} permissions", allPermissions.size());
            }

        } catch (Exception e) {
            log.error("❌ Failed to ensure APEX permissions: {}", e.getMessage(), e);
            throw new RuntimeException("APEX permission sync failed", e);
        }
    }

    private String getRoleDescription(String roleName) {
        return switch (roleName) {
            case "APEX" -> "Apex User who initiates system operations";
            case "SUPERADMIN" -> "Super administrator with full system control";
            case "ADMIN" -> "Administrator with user create access";
            default -> "User role";
        };
    }

    // ==================== RECORD ====================

    public record PermissionSeed(String module, String subModule, String action, String description) {
    }
}