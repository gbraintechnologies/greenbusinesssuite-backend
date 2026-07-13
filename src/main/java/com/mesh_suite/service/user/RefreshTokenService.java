package com.mesh_suite.service.user;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesh_suite.dao.user.RefreshTokenRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.user.RefreshToken;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.exception.TokenRefreshException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.expiration-ms}")
    private long refreshTokenDurationMs;

    public RefreshToken createRefreshToken(String username) {
        Users user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + username));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void revokeAllUserTokens(Users user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs at midnight every day
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllRevokedTokens();
    }
}
