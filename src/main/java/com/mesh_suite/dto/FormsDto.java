package com.mesh_suite.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mesh_suite.util.FormUtils;
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
public class FormsDto implements Serializable {
    private Long id;
    private String name;
    private Long companyId;
    private String description;
    private String url;
    private String redirectUrl;
    private String formInstruction;
    @JsonProperty("formSections")
    private List<FormSectionsDto> formSections;
    private List<String> choiceValues;
    private Boolean userMandatory;
    private String layout;
    private LocalDateTime deadline;
    private FormUtils.PublishStatus publishStatus;
    private Boolean isDeleted;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private LocalDateTime deletedOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignDate;
    private Boolean isTemplate;
    private boolean isAnonymous;
}