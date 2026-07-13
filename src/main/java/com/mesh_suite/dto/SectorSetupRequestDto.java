package com.mesh_suite.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;
@Data
public class SectorSetupRequestDto {
    private Long id;
    private String countryName;
    private List<String> parentSector;
}

