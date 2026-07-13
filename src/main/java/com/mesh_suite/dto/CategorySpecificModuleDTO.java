package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySpecificModuleDTO {
    private Long id;
    private String moduleName;
    private String adminFeatures;
    private String clientFeatures;
    private boolean isTemplate;
}
