package com.mesh_suite.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LegacyCompanyWriteRequest {
    @JsonAlias({"company_data", "companyData"})
    private Map<String, Object> companyData;

    @JsonAlias({"custom_fields", "customFields", "custom_profiles"})
    private List<CustomFieldInput> customFields;
}
