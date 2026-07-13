package com.mesh_suite.service.user;


import com.mesh_suite.dto.Paginate;
import com.mesh_suite.dto.request.RoleRequest;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.domain.user.Permission;
import com.mesh_suite.domain.user.Role;
import com.mesh_suite.dao.user.PermissionRepository;
import com.mesh_suite.dao.user.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

import javax.management.relation.RoleNotFoundException;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository RoleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public Long createRoleWithPermissions(RoleRequest request) {
        // Create or get existing permissions
        Set<Permission> permissions = request.getPermissions().stream()
                .map(permRequest -> {
                    String permissionName = Permission.buildName(
                            permRequest.getModule(),
                            permRequest.getSubModule(),
                            permRequest.getAction()
                    );

                    return permissionRepository.findByName(permissionName)
                            .orElseGet(() -> permissionRepository.save(
                                    Permission.builder()
                                            .name(permissionName)
                                            .module(permRequest.getModule())
                                            .subModule(permRequest.getSubModule())
                                            .action(permRequest.getAction())
                                            .description(permRequest.getDescription())
                                            .build()
                            ));
                })
                .collect(Collectors.toSet());

        // Create and save the role
        Role role = Role.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .permissions(permissions)
                .build();

        return RoleRepository.save(role).getId();
    }
    @Transactional
    public Role updateRoleWithPermissions(Long roleId, RoleRequest request) {
        // Check if role exists
        if (roleId == null) {
            throw new ResourceNotFoundException("Role ID cannot be null");
        }
        
        Role role;

        try {
            role = RoleRepository.findById(roleId)
                    .orElseThrow(() -> new RoleNotFoundException("Role not found"));
        } catch (RoleNotFoundException e) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        // Update basic role info
        if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            role.setRoleName(request.getRoleName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        // Process permission changes
        if (request.getPermissions() != null) {
            // Get names of requested permissions
            Set<String> requestedPermissionNames = request.getPermissions().stream()
                    .map(perm -> Permission.buildName(
                            perm.getModule(),
                            perm.getSubModule(),
                            perm.getAction()))
                    .collect(Collectors.toSet());

            // Remove permissions not in the request
            role.getPermissions().removeIf(
                    existingPerm -> !requestedPermissionNames.contains(existingPerm.getName())
            );

            // Add new permissions
            request.getPermissions().forEach(permRequest -> {
                String permName = Permission.buildName(
                        permRequest.getModule(),
                        permRequest.getSubModule(),
                        permRequest.getAction());

                // Check if permission already exists in role
                boolean permissionExists = role.getPermissions().stream()
                        .anyMatch(p -> p.getName().equals(permName));

                if (!permissionExists) {
                    Permission permission = permissionRepository.findByName(permName)
                            .orElseGet(() -> permissionRepository.save(
                                    Permission.builder()
                                            .name(permName)
                                            .module(permRequest.getModule())
                                            .subModule(permRequest.getSubModule())
                                            .action(permRequest.getAction())
                                            .description(permRequest.getDescription())
                                            .build()));
                    role.addPermission(permission);
                }
            });
        }

        return RoleRepository.save(role);
    }
    public Paginate<Role> getAllRoles(int page, int size) {
        Page<Role> roles = RoleRepository.findAll(PageRequest.of(page, size, Sort.by("roleName")));

        return new Paginate<>(
                roles.getNumber(),
                roles.getSize(),
                roles.getTotalElements(),
                roles.getTotalPages(),
                roles.getContent()
        );

    }

    public Role getRoleById(Long id) {
        return RoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }
    public List<String> getAllRoleNames() {
        return RoleRepository.findAllRoleNames();
    }

    public void deleteRole(Long roleId) {
        Role role;
        try {
            role = RoleRepository.findById(roleId)
                    .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + roleId));
            RoleRepository.delete(role);
        } catch (RoleNotFoundException ex) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }
        
    }
    // get permissions by role name
    public List<Permission> getRolePermission(String roleName) {
        Role role = RoleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
        return new ArrayList<>(role.getPermissions());
    }
}
