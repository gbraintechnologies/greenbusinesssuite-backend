package com.mesh_suite.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class UpdateCompanyFormsDTO {
    @NotNull(message = "Company id is required")
    private Long id;
    @NotNull(message = "Form id list cannot be null")
    private List<Long> assignedFormIds;
}