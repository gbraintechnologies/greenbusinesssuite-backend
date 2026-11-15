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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
        userCompany.setBuildStatus(BuildStatus.PENDING);

        UserCompany company = userCompanyRepository.save(userCompany);

        // No physical database to provision anymore — just seed the tenant's default roles
        // and flip it ACTIVE, atomically with company creation.
        companyMigrationService.provisionTenant(company);
        userCompanyRepository.save(company);

        // Create primary contact as company admin and email Company ID + initial password
        boolean emailSent = provisionPrimaryContactAdmin(company);

        String message = emailSent
                ? "Company setup successful. Registration email with Company ID and initial password sent to primary contact."
                : "Company setup successful. Registration email was not sent (missing contact email or account already exists).";

        return UserCompanyMapper.toRegResp(company, message);
    }

    /**
     * Creates an ADMIN account for the primary contact (if email is present and unused)
     * and emails them the company identifier + temporary password.
     * @return true when a registration email was sent
     */
    private boolean provisionPrimaryContactAdmin(UserCompany company) {
        String email = company.getPrimaryContactEmail();
        if (!StringUtils.hasText(email)) {
            log.warn("Skipping registration email — company {} has no primary contact email", company.getId());
            return false;
        }

        String tenantId = company.getCompanyIdentifier();
        if (!StringUtils.hasText(tenantId)) {
            log.warn("Skipping registration email — company {} has no identifier", company.getId());
            return false;
        }

        if (userRepository.existsByEmail(email.trim())) {
            log.info("Primary contact email {} already exists — sending credentials refresh is skipped; linking if possible", email);
            userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(existing -> {
                if (!StringUtils.hasText(existing.getCompanyIdentifier())) {
                    existing.setCompanyIdentifier(tenantId);
                    userRepository.save(existing);
                }
                if (company.getCompanyAdmin() == null) {
                    company.setCompanyAdmin(existing);
                    userCompanyRepository.save(company);
                }
            });
            return false;
        }

        try {
            Role adminRole = roleRepository.findByRoleNameAndTenantId("ADMIN", tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin role not found for tenant: " + tenantId));

            String rawPassword = codeGenerator.generateTemporaryPassword();
            String[] nameParts = splitContactName(company.getPrimaryContactName());

            Users admin = Users.builder()
                    .firstName(nameParts[0])
                    .lastName(nameParts[1])
                    .username(email.trim())
                    .email(email.trim())
                    .password(passwordEncoder.encode(rawPassword))
                    .phoneNumber(company.getPrimaryContactPhoneNumber())
                    .status(UserStatus.ACTIVE)
                    .roleName("ADMIN")
                    .role(adminRole)
                    .companyIdentifier(tenantId)
                    .isVerified(true)
                    .enabled(true)
                    .createdOn(LocalDateTime.now())
                    .build();

            userRepository.saveAndFlush(admin);
            company.setCompanyAdmin(admin);
            userCompanyRepository.save(company);

            emailService.sendCompanyRegistrationEmail(
                    admin.getEmail(),
                    admin.getFirstName(),
                    company.getCompanyName(),
                    tenantId,
                    rawPassword
            );
            log.info("Registration email sent for company {} to {}", tenantId, admin.getEmail());
            return true;
        } catch (Exception e) {
            // Company creation should still succeed even if invite email fails
            log.error("Failed to provision primary contact admin for company {}: ", company.getId(), e);
            return false;
        }
    }

    private String[] splitContactName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return new String[]{"Admin", "User"};
        }
        String trimmed = fullName.trim();
        int space = trimmed.indexOf(' ');
        if (space < 0) {
            return new String[]{trimmed, "Admin"};
        }
        return new String[]{
                trimmed.substring(0, space).trim(),
                trimmed.substring(space + 1).trim()
        };
    }

    public Paginate<CompanyResponseDTO> getAllCompanies(Pageable pageable) {
        Page<UserCompany> companies = userCompanyRepository.findAllNotDeleted(pageable);
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
        if (Boolean.TRUE.equals(company.getIsDeleted())) {
            throw new ResourceNotFoundException("Company not found with id: " + id);
        }
        return UserCompanyMapper.toResponseDto(company);
    }

    public CompanyResponseDTO getByIdentifier(String companyIdentifier) {
        UserCompany company = userCompanyRepository.findByCompanyIdentifier(companyIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with identifier: " + companyIdentifier));
        if (Boolean.TRUE.equals(company.getIsDeleted())) {
            throw new ResourceNotFoundException("Company not found with identifier: " + companyIdentifier);
        }
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

    @Transactional
    public MessageResponse deleteCompany(Long id) {
        UserCompany company = userCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        if (Boolean.TRUE.equals(company.getIsDeleted())) {
            return new MessageResponse("Company already deleted");
        }

        company.setIsDeleted(true);
        company.setDeletedOn(ZonedDateTime.now());
        company.setStatus(CompanyStatus.INACTIVE);
        userCompanyRepository.save(company);

        log.info("Soft-deleted company id={} identifier={}", company.getId(), company.getCompanyIdentifier());
        return new MessageResponse("Company deleted successfully");
    }

    @Transactional
    public MessageResponse assignTenantAdmin(CreateUserRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            Role adminRole = roleRepository.findByRoleNameAndTenantId("ADMIN", tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin role not found for tenant: " + tenantId));

            String rawPassword = codeGenerator.generateTemporaryPassword();

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

            var companyOpt = userCompanyRepository.findByCompanyIdentifier(tenantId);
            if (companyOpt.isPresent()) {
                UserCompany company = companyOpt.get();
                if (company.getCompanyAdmin() == null) {
                    company.setCompanyAdmin(user);
                    userCompanyRepository.save(company);
                }
                emailService.sendCompanyRegistrationEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        company.getCompanyName(),
                        tenantId,
                        rawPassword
                );
            } else {
                emailService.sendTemporaryPasswordEmail(user, rawPassword);
            }

            log.info("Admin user created successfully for tenant: {}", tenantId);
            return new MessageResponse("Admin user assigned successfully! Company ID and temporary password sent.");
        } catch (Exception e) {
            log.error("Failed to create tenant admin:", e);
            throw new RuntimeException("Failed to create tenant admin");
        }
    }
}
