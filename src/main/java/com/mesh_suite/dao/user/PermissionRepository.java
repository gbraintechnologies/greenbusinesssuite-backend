package com.mesh_suite.dao.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mesh_suite.domain.user.Permission;

public interface PermissionRepository extends JpaRepository<Permission,Long> {
    boolean existsByName(String name);
    Optional<Permission> findByName(String name);
}
