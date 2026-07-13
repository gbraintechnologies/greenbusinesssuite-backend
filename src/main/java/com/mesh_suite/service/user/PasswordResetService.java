package com.mesh_suite.service.user;

import com.mesh_suite.dao.user.PasswordResetTokenRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.user.PasswordResetToken;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.request.ForgotPasswordRequest;
import com.mesh_suite.dto.request.ResetPasswordRequest;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.exception.UnAuthenticatedException;
import com.mesh_suite.service.notify.EmailService;
import com.mesh_suite.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CodeGenerator codeGenerator;

    @Transactional
    public MessageResponse requestPasswordReset(ForgotPasswordRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String token = generateResetToken();

        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        passwordResetTokenRepository.save(resetToken);

        String temPass= codeGenerator.generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temPass));
        userRepository.save(user);
        emailService.sendTemporaryPasswordEmail(user, temPass);

        return new MessageResponse("Check your email for a temporary password to log into account setting to request a password change.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new UnAuthenticatedException("Invalid password reset token"));

        if (resetToken.isExpired()) {
            throw new UnAuthenticatedException("Password reset token is expired");
        }

        Users user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse("Password has been set successfully. You can now log in.");
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}