package com.mesh_suite.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserCompanyDTO {
    @NotBlank(message = "Company identifier cannot be blank")
    private String companyIdentifier;
}