package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticResult {
    private String fieldName;
    private String function;
    private String displayType;
    private List<Map<String, Object>> data;
}
