package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBrandingDTO {
    private String tenancyId;
    private Long companyId;
    private String companyName;
    private String logo;
    private String color;
    private Set<Long> moduleIds;
    private Set<Long> categorySpecificModuleIds;

}
