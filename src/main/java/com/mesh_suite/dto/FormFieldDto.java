package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormFieldDto implements Serializable {
    private Long id;
    private String name;
    private String label;
    private String placeHolder;
    private String description;
    private String instruction;
    private Integer ordering;
    private String fieldDataType;
    private Long maxLength;
    private Boolean horizontalAlign;
    private List<String> choiceValue;
    private Boolean isMandatory;
    private String validPattern;
    private Boolean isDeleted;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private LocalDateTime deletedOn;

    private Boolean isStatisticalField;
    private String statisticalFunction;
    private String displayType;
}
