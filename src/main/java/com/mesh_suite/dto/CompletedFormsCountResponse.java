package com.mesh_suite.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompletedFormsCountResponse {
    private boolean first;
    private boolean last;
    private long totalElements;
    private int totalPages;
    private int size;
    private List<Map<String, Long>> userFormStatList;
}
