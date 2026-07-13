package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildAddressSchemeEntriesDto {
    private Long id;
    private String name;
    private Long parentAddressSchemeEntriesId;
}