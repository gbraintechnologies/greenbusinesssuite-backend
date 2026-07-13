package com.mesh_suite.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mesh_suite.util.FormUtils;

import java.time.LocalDateTime;

public interface FormProjection {
    Long getId();
    String getName();
    Long getCompanyId();
    String getUrl();
    String getDescription();
    String getFormInstruction();
    Boolean getUserMandatory();
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime getDeadline();
    FormUtils.PublishStatus getPublishStatus();
    Boolean getIsDeleted();
    Boolean getIsTemplate();
    String getLayout();
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime getCreatedOn();
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime getUpdatedOn();
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime getDeletedOn();
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime getAssignDate();
    Boolean getIsAnonymous();
    Boolean getMultipleForms();
    String getRedirectUrl();
}