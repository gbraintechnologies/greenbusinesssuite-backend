package com.mesh_suite.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBrandingDetailsDTO {
    private Long id;
    private String tenancyId;
    private Long companyId;
    private String companyName;
    private String logo;
    private String color;

    private Set<ModuleDTO> modules;
    private Set<CategorySpecificModuleDto> categorySpecificModules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleDTO {
        private Long id;
        private String moduleName;
        private String moduleDescription;
        private String adminFeatures;
        private String clientFeatures;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySpecificModuleDto {
        private Long id;
        private String moduleName;
        private String adminFeatures;
        private String clientFeatures;
        private boolean isTemplate;
    }
}
