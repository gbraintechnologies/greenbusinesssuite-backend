package com.mesh_suite.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class CustomFieldInput {
    @JsonAlias({"custom_profile_item_id", "customProfileItemId", "item_id"})
    private Long customProfileItemId;
    private String value;
}
