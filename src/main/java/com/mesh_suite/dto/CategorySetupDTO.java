package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySetupDTO {
    private Long id;
    private String categoryName;
    private String categoryDescription;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private Set<CategorySpecificModuleDTO> categorySpecificModules;

}
