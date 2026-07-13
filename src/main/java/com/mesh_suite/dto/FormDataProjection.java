package com.mesh_suite.dto;

import com.mesh_suite.constant.forms.FormResponseStatus;

import java.time.LocalDateTime;

public interface FormDataProjection {
    Long getId();
    Long getFormId();
    Boolean getIsCompleted();
    Long getCompanyId();
    Long getUserId();
    FormResponseStatus getStatus();
    LocalDateTime getCreatedOn();
    LocalDateTime getUpdatedOn();
}