package com.mesh_suite.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCompanyAdminDTO {
    @NotNull(message = "Company ID cannot be null")
    private Long id;
    
    @NotNull(message = "New admin user ID cannot be null")
    private Long newAdminUserId;
}