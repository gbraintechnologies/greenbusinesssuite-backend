package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentAddressSchemeEntriesDto {
    private Long id;
    private String name;
    private List<ChildAddressSchemeEntriesDto> childEntries = new ArrayList<>();
}