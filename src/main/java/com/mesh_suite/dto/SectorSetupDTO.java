package com.mesh_suite.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SectorSetupDTO {
    private Long id;
    private String countryName;
    private int parentSectorCount;
    private int subSectorCount;
}

