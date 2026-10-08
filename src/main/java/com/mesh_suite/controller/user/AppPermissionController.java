package com.mesh_suite.controller.user;

import com.mesh_suite.domain.user.Role;
import com.mesh_suite.dto.request.PermissionIdsRequest;
import com.mesh_suite.exception.BadRequestException;
import com.mesh_suite.service.user.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mesh-suite/v1.0/apps")
@RequiredArgsConstructor
public class AppPermissionController {

    private final RoleService roleService;

    @Operation(summary = "Replace the permissions assigned to a role")
    @PostMapping("/permissions/update_multi_permissions_for_role/{roleId}")
    public ResponseEntity<Role> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody PermissionIdsRequest request) {
        if (request == null || request.getPermissionIds() == null) {
            throw new BadRequestException("permission_ids is required");
        }
        return ResponseEntity.ok(roleService.replacePermissions(roleId, request.getPermissionIds()));
    }
}
