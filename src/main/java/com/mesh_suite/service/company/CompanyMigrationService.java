package com.mesh_suite.service.company;

import com.mesh_suite.constant.company.BuildStatus;
import com.mesh_suite.dao.user.RoleRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.domain.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyMigrationService {

    private final RoleRepository roleRepository;

    public void provisionTenant(UserCompany company) {
        String tenantId = company.getCompanyIdentifier();
        log.info("Provisioning tenant: {}", tenantId);

        roleRepository.save(Role.builder()
                .roleName("ADMIN")
                .description("Full access")
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build());
        roleRepository.save(Role.builder()
                .roleName("CLIENT")
                .description("Limited access")
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build());

        company.setBuildStatus(BuildStatus.ACTIVE);
        log.info("Tenant provisioned: {}", tenantId);
    }
}
