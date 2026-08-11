package com.mesh_suite.service.company;

import com.mesh_suite.constant.company.BuildStatus;
import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.dao.user.RoleRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.domain.user.Role;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.CompanyRegResp;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.dto.request.*;
import com.mesh_suite.dto.response.CompanyResponseDTO;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.interceptor.TenantContext;
import com.mesh_suite.mapper.UserCompanyMapper;
import com.mesh_suite.service.notify.EmailService;
import com.mesh_suite.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyDetailService {

    private final UserCompanyRepository userCompanyRepository;
    private final CompanyMigrationService companyMigrationService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CodeGenerator codeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${spring.datasource.url}")
    private String masterDbUrl;

    @Value("${spring.datasource.username:}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Transactional
    public CompanyRegResp createCompany(CompanyCreateDTO request) throws UnsupportedEncodingException {
        if (request == null) {
            throw new IllegalArgumentException("CompanyCreateDTO cannot be null");
        }

        Users companyAdmin = null;
        if (request.getCompanyAdminId() != null) {
            companyAdmin = userRepository.findById(request.getCompanyAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with ID: " + request.getCompanyAdminId()));
        }

        UserCompany userCompany = UserCompanyMapper.toEntity(request, companyAdmin);

        if (userCompany.getStatus() == null) {
            userCompany.setStatus(request.getStatus());
        }

        userCompany.generateAndSetCompanyIdentifier();

        // Derive host and port from the master datasource URL — works on any environment
        String[] hostPort = extractHostAndPort(masterDbUrl);
        userCompany.setupDatabaseConfig(
                hostPort[0],
                Integer.parseInt(hostPort[1]),
                dbUser,
                dbPassword
        );

        // Mark PENDING before async provisioning so the UI is not stuck on null buildStatus
        if (userCompany.getBuildStatus() == null) {
            userCompany.setBuildStatus(BuildStatus.PENDING);
        }

        UserCompany company = userCompanyRepository.save(userCompany);
        Long companyId = company.getId();

        // Provision only after the company row is committed — @Async can otherwise race the TX
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    companyMigrationService.provisionTenantDatabase(companyId);
                }
            });
        } else {
            companyMigrationService.provisionTenantDatabase(companyId);
        }

        return UserCompanyMapper.toRegResp(company, "Company setup successful, resource creation started");
    }

    private String[] extractHostAndPort(String jdbcUrl) {
        // Parses: jdbc:postgresql://host:5432/dbname
        try {
            String withoutProtocol = jdbcUrl.substring(jdbcUrl.indexOf("://") + 3);
            String hostPortPart = withoutProtocol.substring(0, withoutProtocol.indexOf('/'));
            String[] parts = hostPortPart.split(":");
            return parts.length == 2
                    ? new String[]{parts[0], parts[1]}
                    : new String[]{parts[0], "5432"};
        } catch (Exception e) {
            log.error("Failed to extract host/port from JDBC URL: {}", jdbcUrl, e);
            return new String[]{"localhost", "5432"};
        }
    }

    // ── everything below is unchanged ────────────────────────────────────────

    public Paginate<CompanyResponseDTO> getAllCompanies(Pageable pageable) {
        Page<UserCompany> companies = userCompanyRepository.findAll(pageable);
        return toPaginate(companies, UserCompanyMapper::toResponseDto);
    }

    public Paginate<CompanyResponseDTO> getCompaniesByUserIdentifier(String companyIdentifier, Pageable pageable) {
        Page<UserCompany> companies = userCompanyRepository.findByCompanyAdmin_CompanyIdentifier(companyIdentifier, pageable);
        return toPaginate(companies, UserCompanyMapper::toResponseDto);
    }

    public Paginate<CompanyResponseDTO> getCompaniesByUserId(Long userId, Pageable pageable) {
        Page<UserCompany> companies = userCompanyRepository.findByCompanyAdmin_Id(userId, pageable);
        return toPaginate(companies, UserCompanyMapper::toResponseDto);
    }

    public CompanyResponseDTO getById(Long id) {
        UserCompany company = userCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return UserCompanyMapper.toResponseDto(company);
    }

    public CompanyResponseDTO searchByName(String name) {
        UserCompany company = userCompanyRepository.findByCompanyNameContainingIgnoreCase(name);
        return UserCompanyMapper.toResponseDto(company);
    }

    public Paginate<CompanyResponseDTO> filterByStatus(CompanyStatus status, Pageable pageable) {
        Page<UserCompany> companies = userCompanyRepository.findByStatus(status, pageable);
        return toPaginate(companies, UserCompanyMapper::toResponseDto);
    }

    public CompanyUpdateDTO update(CompanyUpdateDTO companyDetailDTO) {
        UserCompany existingCompany = userCompanyRepository.findById(companyDetailDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyDetailDTO.getId()));
        UserCompanyMapper.updateEntityFromDto(companyDetailDTO, existingCompany);
        UserCompany updatedCompany = userCompanyRepository.save(existingCompany);
        return UserCompanyMapper.toUpdateDto(updatedCompany);
    }

    private <T, R> Paginate<R> toPaginate(Page<T> page, java.util.function.Function<T, R> mapper) {
        return new Paginate<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent().stream().map(mapper).collect(Collectors.toList())
        );
    }

    @Transactional
    public CompanyUpdateDTO updateCompanyStatus(UpdateCompanyStatusDTO dto) {
        UserCompany company = userCompanyRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + dto.getId()));
        company.setStatus(dto.getStatus());
        userCompanyRepository.save(company);
        return UserCompanyMapper.toUpdateDto(company);
    }

    @Transactional
    public CompanyUpdateDTO updateCompanyFormSet(UpdateCompanyFormsDTO dto) {
        UserCompany company = userCompanyRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + dto.getId()));
        company.setAssignedFormIds(dto.getAssignedFormIds());
        userCompanyRepository.save(company);
        return UserCompanyMapper.toUpdateDto(company);
    }

    public MessageResponse assignTenantAdmin(CreateUserRequest request) {
        try {
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseThrow(() -> new ResourceNotFoundException("Admin role not found"));

            String rawPassword = codeGenerator.generateTemporaryPassword();
            String tenantId = TenantContext.getCurrentTenant();

            Users user = Users.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(rawPassword))
                    .phoneNumber(request.getPhone())
                    .profileImage(request.getProfileImage())
                    .status(UserStatus.ACTIVE)
                    .roleName("ADMIN")
                    .role(adminRole)
                    .companyIdentifier(tenantId)
                    .isVerified(true)
                    .enabled(true)
                    .createdOn(LocalDateTime.now())
                    .build();

            userRepository.saveAndFlush(user);
            log.info("Admin user created successfully for tenant: {}", tenantId);
            emailService.sendTemporaryPasswordEmail(user, rawPassword);

            return new MessageResponse("Admin user assigned successfully! Temporary password sent.");
        } catch (Exception e) {
            log.error("Failed to create tenant admin:", e);
            throw new RuntimeException("Failed to create tenant admin");
        }
    }
}