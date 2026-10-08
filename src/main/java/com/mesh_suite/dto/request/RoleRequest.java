package com.mesh_suite.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {

    @NotBlank(message = "role name cannot be blank")
    private String roleName;
    
    private String description;

    @Valid
    private List<PermissionRequest> permissions;
}
