package com.mesh_suite.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomFieldValueView {
    private final Long id;
    private final Long customProfileItemId;
    private final String value;

    public CustomFieldValueView(Long id, Long customProfileItemId, String value) {
        this.id = id;
        this.customProfileItemId = customProfileItemId;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @JsonProperty("customProfileItemId")
    public Long getCustomProfileItemId() {
        return customProfileItemId;
    }

    @JsonProperty("custom_profile_item_id")
    public Long getCustom_profile_item_id() {
        return customProfileItemId;
    }
}
