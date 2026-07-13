package com.mesh_suite.controller.user;

import com.mesh_suite.dto.request.*;
import com.mesh_suite.dto.response.JwtResponse;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.service.user.AuthService;
import com.mesh_suite.service.notify.EmailService;
import com.mesh_suite.service.user.PasswordResetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/mesh-suite/v1.0/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;

    @PostMapping("/sign-in")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String tenantId = request.getHeader("tenantid");
        log.info("🔐 Login request received - Email: {}, Tenant Header: {}",
                loginRequest.getEmail(), tenantId);

        JwtResponse jwtResponse = authService.login(loginRequest, tenantId);
        log.info("✅ Login response generated for: {}", loginRequest.getEmail());
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/apex-sign-up")
    @PreAuthorize("hasRole('APEX')")
    public ResponseEntity<MessageResponse> apexregister(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        MessageResponse userRegistered = authService.register(registerRequest, request);
        return ResponseEntity.ok(userRegistered);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        MessageResponse userRegistered = authService.register(registerRequest, request);
        return ResponseEntity.ok(userRegistered);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.requestPasswordReset(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }

    @GetMapping("/verify-account")
    public ResponseEntity<MessageResponse> verifyAccount(@RequestParam("email") String email) {
        MessageResponse response = authService.verifyAccount(email);
        return ResponseEntity.ok(response);
    }
}