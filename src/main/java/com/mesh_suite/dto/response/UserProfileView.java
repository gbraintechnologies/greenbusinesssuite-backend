package com.mesh_suite.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileView {
    private final Long roleId;
    private final String roleName;

    @JsonProperty("app_id")
    public long getAppId() {
        return 1L;
    }

    @JsonProperty("role_id")
    public Long getRole_id() {
        return roleId;
    }

    @JsonProperty("role_name")
    public String getRole_name() {
        return roleName;
    }
}
