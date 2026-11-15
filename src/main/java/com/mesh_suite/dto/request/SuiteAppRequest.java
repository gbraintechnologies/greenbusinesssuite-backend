package com.mesh_suite.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuiteAppRequest {

    @NotBlank(message = "App name is required")
    private String name;

    private String tagline;

    private String description;

    @NotBlank(message = "Website link is required")
    private String href;

    private String category;

    private Boolean published;

    private String logoUrl;

    private String mark;

    private String accent;

    private String wash;

    private Integer sortOrder;
}
