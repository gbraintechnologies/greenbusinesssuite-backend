package com.mesh_suite.controller.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesh_suite.domain.user.Role;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.dto.request.RoleRequest;
import com.mesh_suite.service.user.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mesh-suite/v1.0/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @Operation(summary = "Create new Role and Permissions")
    @PostMapping("/permission/create")
    public ResponseEntity<Long> createRole(@Valid @RequestBody RoleRequest request) {
        return  ResponseEntity.ok(roleService.createRoleWithPermissions(request));
    }
    @Operation(summary = "Update Role and Permissions")
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody RoleRequest role) {
        return ResponseEntity.ok(roleService.updateRoleWithPermissions(id, role));
    }

    @Operation(summary = "Get roles and permissions by Id")
    @GetMapping("/permission-by-id/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }
    @Operation(summary = "Retrieve all roles and permissions")
    @GetMapping("/permission/all")
    public ResponseEntity<Paginate<Role>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Paginate<Role> response = roleService.getAllRoles(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve all roles available in the system")
    @GetMapping("/role-names")
    public ResponseEntity<List<String>> getAllRoleNames() {
        List<String> roleNames = roleService.getAllRoleNames();
        return ResponseEntity.ok(roleNames);
    }
    @Operation(summary = "Delete role by ID")
    @DeleteMapping("/{roleId}")
    public ResponseEntity<String> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok("Role deleted successfully.");
    }
}
