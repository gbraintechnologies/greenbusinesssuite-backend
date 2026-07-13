package com.mesh_suite.dto.request;

import com.mesh_suite.constant.company.CompanyStatus;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class UpdateCompanyStatusDTO {
    @NotNull(message = "Company id is required")
    private Long id; // assuming your PK type
    @NotNull(message = "Status is required")
    private CompanyStatus status; // Your enum type
}