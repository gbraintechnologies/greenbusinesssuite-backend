package com.mesh_suite.dto.response;

import java.sql.Time;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mesh_suite.constant.company.BuildStatus;
import com.mesh_suite.constant.forms.CompanyCurrency;
import com.mesh_suite.constant.company.CompanyStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponseDTO {

    private Long id;

    private String description;

    @NotBlank(message = "Company name is required")
    private String companyName;

    private CompanyStatus status;

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
    private String companyIdentifier;

    private List<Long> assignedFormIds;

    private String smsSenderId;

    private List<CustomFieldValueView> companyCustomValues;

    @JsonProperty("company_name")
    public String getCompany_name() {
        return companyName == null ? "" : companyName;
    }

    @JsonProperty("company_identifier")
    public String getCompany_identifier() {
        return companyIdentifier == null ? "" : companyIdentifier;
    }

    @JsonProperty("company_logo")
    public String getCompany_logo() {
        return companyLogo;
    }

    @JsonProperty("company_address")
    public String getCompany_address() {
        return companyAddress;
    }

    @JsonProperty("company_code")
    public String getCompany_code() {
        return companyCode;
    }

    @JsonProperty("primary_contact_name")
    public String getPrimary_contact_name() {
        return primaryContactName == null ? "" : primaryContactName;
    }

    @JsonProperty("primary_contact_email")
    public String getPrimary_contact_email() {
        return primaryContactEmail == null ? "" : primaryContactEmail;
    }

    @JsonProperty("primary_contact_phone_number")
    public String getPrimary_contact_phone_number() {
        return primaryContactPhoneNumber == null ? "" : primaryContactPhoneNumber;
    }

    @JsonProperty("primary_currency")
    public String getPrimary_currency() {
        return primaryCurrency == null ? null : primaryCurrency.name();
    }

    @JsonProperty("company_sms_sender_id")
    public String getCompany_sms_sender_id() {
        return smsSenderId;
    }

    @JsonProperty("company_custom_values")
    public List<CustomFieldValueView> getCompany_custom_values() {
        return companyCustomValues == null ? List.of() : companyCustomValues;
    }
}