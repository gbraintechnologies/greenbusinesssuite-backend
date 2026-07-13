package com.mesh_suite.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.mesh_suite.constant.company.CompanyStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRegResp {
    private Long id;
    private String companyName;
    private CompanyStatus status;
    private String primaryContactName;
    private String primaryContactEmail;
    private String companyCode;
    private List<Long> assignedFormIds;
    private String message;
}