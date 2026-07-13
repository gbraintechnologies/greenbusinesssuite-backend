package com.mesh_suite.dao.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mesh_suite.domain.user.Token;

import java.time.LocalDateTime;
import java.util.Optional;
@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByAccessToken(String token);


    @Modifying
    @Transactional
    @Query("""
    DELETE FROM Token t
    WHERE t.loggedOut = true
       OR t.createdOn < :oneDayAgo
""")
    int deleteOldAndLoggedOutTokens(@Param("oneDayAgo") LocalDateTime oneDayAgo);

}
