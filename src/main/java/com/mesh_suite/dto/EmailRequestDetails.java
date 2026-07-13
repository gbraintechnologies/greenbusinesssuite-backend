package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDetails {
    private String userFullName;
    private String companyAdminFirstName;
    private String companyAdminEmail;
    private Long formId;
}
