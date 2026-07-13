package com.mesh_suite.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RoleRequest {

    @NotBlank(message = "role name cannot be blank")
    private String roleName;
    
    private String description;

    @Valid
    @NotEmpty
    private List<PermissionRequest> permissions;
}
