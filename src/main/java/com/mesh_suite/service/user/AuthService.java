package com.mesh_suite.service.user;


import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.dao.user.PasswordResetTokenRepository;
import com.mesh_suite.dao.user.RoleRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.user.RefreshToken;
import com.mesh_suite.domain.user.Role;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.request.LoginRequest;
import com.mesh_suite.dto.request.RefreshTokenRequest;
import com.mesh_suite.dto.request.RegisterRequest;
import com.mesh_suite.dto.response.JwtResponse;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.exception.DuplicateResourceException;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.exception.TokenRefreshException;
import com.mesh_suite.interceptor.TenantContext;
import com.mesh_suite.security.JwtTokenProvider;
import com.mesh_suite.security.MasterTenantValidator;
import com.mesh_suite.service.notify.EmailService;
import com.mesh_suite.util.CodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository userRoleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final CodeGenerator codeGenerator;
    private final MasterTenantValidator masterTenantValidator;


    private Role getRole(Long id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User role not found"));
    }

    public JwtResponse login(LoginRequest loginRequest, String tenantHeader) {
        String requestedTenant = StringUtils.hasText(tenantHeader) ? tenantHeader : AppConstants.DEFAULT_TENANT_ID;
        log.info(" ===== LOGIN START =====");

        try {

            TenantContext.setCurrentTenant(requestedTenant);
            log.info(" Tenant context set to: {}", requestedTenant);

            log.info(" Attempting authentication...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            log.info(" Authentication successful! Principal: {}", authentication.getPrincipal());

            // Get the authenticated user
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.info(" UserDetails username: {}", userDetails.getUsername());

            // Find the user in the database
            Users user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));
            log.info(" User found in database: {}", user.getEmail());

            // Generate JWT tokens
            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshTokenStr = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

            // Determine the final tenant
            String finalTenant = requestedTenant;
            if (!StringUtils.hasText(finalTenant) || AppConstants.DEFAULT_TENANT_ID.equals(finalTenant)) {
                finalTenant = user.getCompanyIdentifier();
                log.info(" Using user's company identifier as tenant: {}", finalTenant);
            }

            JwtResponse response = JwtResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenStr)
                    .tenantId(finalTenant)
                    .roleName(user.getRoleName())
                    .roleId(user.getRole() != null ? user.getRole().getId() : null)
                    .build();

            log.info(" ===== LOGIN SUCCESSFUL =====");
            return response;

        } catch (BadCredentialsException e) {
            log.warn(" Login failed - Bad credentials for: {}", loginRequest.getEmail());
            log.warn("Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(" Login error for user: {}", loginRequest.getEmail(), e);
            throw new BadCredentialsException("Invalid email or password");
        } finally {
            log.info(" ===== LOGIN END =====");
        }
    }

    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtTokenProvider.createAccessTokenFromUsername(user.getUsername());

                    return JwtResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(requestRefreshToken)
                            .id(user.getId())
                            .email(user.getUsername())
                            .firstName(user.getFirstName())
                            .roleName(user.getRoleName())
                            .roleId(user.getId())
                            .lastName(user.getLastName())
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not found in database!"));
    }


    @Transactional
    public MessageResponse register(RegisterRequest registerRequest,  HttpServletRequest request) {
        try {
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new DuplicateResourceException("Email is already in use!");
            }

            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                throw new DuplicateResourceException("Username is already in use!");
            }

            Role userRole = getRole(registerRequest.getRoleId());
            boolean isPrivilegedUser = isPrivilegedRole(userRole.getRoleName());

            String rawPassword = isPrivilegedUser
                    ? codeGenerator.generateTemporaryPassword()
                    : registerRequest.getPassword();

            Users user = Users.builder()
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(rawPassword))
                    .roleName(userRole.getRoleName())
                    .role(userRole)
                    .companyIdentifier(registerRequest.getCompanyIdentifier())
                    .profileImage(registerRequest.getProfile_image())
                    .phoneNumber(registerRequest.getPhone())
                    .status(registerRequest.getStatus() != null ? registerRequest.getStatus() : UserStatus.ACTIVE)
                    .isVerified(isPrivilegedUser) // auto-verified
                    .enabled(true)
                    .createdOn(LocalDateTime.now())
                    .build();


            userRepository.saveAndFlush(user);

            // Delete any previous tokens for this user
            passwordResetTokenRepository.deleteByUser(user);

            // Send email asynchronously, but catch and log failures without failing registration
            try {
                if (isPrivilegedUser) {
                    emailService.sendTemporaryPasswordEmail(user, rawPassword);
                    return new MessageResponse("User registered successfully! A temporary password has been sent to your email. If not received, contact support.");
                } else {
                    emailService.sendVerificationLinkEmail(user, request);
                    return new MessageResponse("User registered successfully! Please check your email to activate your account. If not received, contact support.");
                }
            } catch (Exception emailEx) {
                log.error("Failed to send registration email for user {}: {}", user.getEmail(), emailEx.getMessage(), emailEx);
                return new MessageResponse("User registered successfully, but email sending failed. Please contact support for assistance.");
            }
        } catch (DataIntegrityViolationException e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            throw new RuntimeException("Registration failed due to data constraints: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            log.error("Unexpected registration error: {}", e.getMessage(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }


    @Transactional
    public MessageResponse verifyAccount(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isVerified()) {
            return new MessageResponse("Account already verified.");
        }

        user.setVerified(true);
        userRepository.save(user);
        return new MessageResponse("Account verified successfully!");
    }


    @Transactional
    public MessageResponse logout(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(token -> {
                    refreshTokenService.revokeAllUserTokens(token.getUser());
                    return new MessageResponse("Logout successful!");
                })
                .orElseThrow(() -> new TokenRefreshException(request.getRefreshToken(),
                        "Refresh token is not found in database!"));
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    /**
     * Helper method to determine privileged roles.
     */
    private boolean isPrivilegedRole(String roleName) {
        return List.of("SUPERADMIN", "SUPER", "ADMIN").contains(roleName.toUpperCase());
    }
}