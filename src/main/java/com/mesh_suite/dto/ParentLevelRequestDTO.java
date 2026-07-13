package com.mesh_suite.dto;

import lombok.Data;

import java.util.List;
@Data
public class ParentLevelRequestDTO {
    private Long countryId;
    private String parentName;
    private List<String> childLevels;
}
