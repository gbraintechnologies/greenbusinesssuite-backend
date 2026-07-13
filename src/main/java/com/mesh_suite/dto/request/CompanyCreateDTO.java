package com.mesh_suite.dto.request;

import com.mesh_suite.constant.company.BuildStatus;
import com.mesh_suite.constant.forms.CompanyCurrency;
import com.mesh_suite.constant.company.CompanyStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyCreateDTO {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private CompanyStatus status;

    private String description;

    @NotBlank(message = "Primary contact name is required")
    private String primaryContactName;

    @NotBlank(message = "Primary contact email is required")
    private String primaryContactEmail;

    private String primaryContactPhoneNumber;
    private String companyLogo;
    private String companyAddress;
    private String companyDigitalAddress;
    private String industry;
    private String companyMerchantMomoNumber;
    private String companyBankName;
    private String taxId;
    private Time startOfDayTime;
    private Time endOfDayTime;

    private CompanyCurrency primaryCurrency;
    private List<CompanyCurrency> secondaryCurrency;
    
    private Long companyAdminId;
    private String companyCode;
    private BuildStatus buildStatus;
    private String driverName;
    private String dbUrl;

    // List of assigned form IDs
    private List<Long> assignedFormIds;
}