package com.mesh_suite.dto;

import com.mesh_suite.domain.form.FormData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormDataResponse {
    private boolean isFirst;
    private boolean isLast;
    private long totalElements;
    private int totalPages;
    private int size;
    private List<FormData> content;

}
