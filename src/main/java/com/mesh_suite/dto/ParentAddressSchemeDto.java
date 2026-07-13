package com.mesh_suite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentAddressSchemeDto {
    private Long id;
    private String name;
    private JurisdictionDto jurisdiction;
    private String inputType;
}