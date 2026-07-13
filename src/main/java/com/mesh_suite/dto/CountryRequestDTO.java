package com.mesh_suite.dto;

import com.mesh_suite.constant.forms.InputType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CountryRequestDTO {
    private String countryName;
    private Long countryId;
    private String parentLevelName;
    private String childLevelName;
    private InputType inputType;
    private List<String> parentNames;


}
