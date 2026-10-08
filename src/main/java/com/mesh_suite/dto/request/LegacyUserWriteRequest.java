package com.mesh_suite.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LegacyUserWriteRequest {
    @JsonAlias({"user_data", "userData"})
    private Map<String, Object> userData;

    @JsonAlias({"custom_profiles", "customProfiles", "custom_fields", "customFields"})
    private List<CustomFieldInput> customProfiles;
}
