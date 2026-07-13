package com.mesh_suite.dao.user;

import com.mesh_suite.domain.user.RefreshToken;
import com.mesh_suite.domain.user.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUser(Users user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllUserTokens(Users user);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.revoked = true")
    void deleteAllRevokedTokens();
}
