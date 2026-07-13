package com.mesh_suite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormSectionsDto implements Serializable {
    private Long id;
    private String name;
    private Integer ordering;
    private String description;
    private String instruction;
    @JsonProperty("formSections")
    private List<FormFieldDto> formFields = new ArrayList<>();
    private Boolean isDeleted;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private LocalDateTime deletedOn;
}
