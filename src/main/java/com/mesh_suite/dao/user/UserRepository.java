package com.mesh_suite.dao.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.domain.user.Role;
import com.mesh_suite.domain.user.Users;

public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Users> findByPhoneNumber(String phoneNumber);
    Optional<Users> findByResetCode(String code);

    boolean existsByRoleName(String roleName);
    @Query("SELECT u FROM Users u JOIN FETCH u.role WHERE u.role.roleName = :roleName")
    List<Users> findByRoleName(@Param("roleName") String roleName);
    Page<Users> findByRoleName(String roleName, Pageable pageable);
    Page<Users> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    Page<Users> findByStatus(UserStatus status, Pageable pageable);

    boolean existsByRole(Role role);

    @Query("""
            SELECT u FROM Users u
            WHERE LOWER(COALESCE(u.firstName, '')) LIKE LOWER(CONCAT('%', :word, '%'))
               OR LOWER(COALESCE(u.lastName, '')) LIKE LOWER(CONCAT('%', :word, '%'))
               OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :word, '%'))
               OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :word, '%'))
               OR LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :word, '%'))
            """)
    List<Users> searchByWord(@Param("word") String word);

    Optional<Users> findByEmailIgnoreCase(String email);

    long countByStatus(UserStatus status);

    @Query("SELECT COUNT(u) FROM Users u WHERE u.createdOn >= :from")
    long countCreatedSince(@Param("from") LocalDateTime from);

    @Query("SELECT MONTH(u.createdOn), COUNT(u) FROM Users u WHERE u.createdOn >= :from GROUP BY MONTH(u.createdOn)")
    List<Object[]> countCreatedByMonthSince(@Param("from") LocalDateTime from);

    @Query("SELECT COUNT(u) FROM Users u WHERE u.isVerified = true")
    long countVerified();
}