package com.mesh_suite.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionRequest {
    @NotBlank
    private String module;
    
    private String subModule;
    
    @NotBlank
    private String action;
    
    private String description;
}