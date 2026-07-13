package com.mesh_suite.dao.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mesh_suite.domain.user.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    boolean existsByRoleName(String roleName);
    boolean existsByIdAndRoleName(Long roleId, String roleName);
    Optional<Role> findByRoleName(String roleName);

    @Query("SELECT r.roleName FROM Role r ORDER BY r.roleName ASC")
    List<String> findAllRoleNames();
    Optional<Role> findByRoleNameIgnoreCase(String roleName);
}