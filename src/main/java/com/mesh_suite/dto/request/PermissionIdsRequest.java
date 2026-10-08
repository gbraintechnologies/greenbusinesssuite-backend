package com.mesh_suite.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class PermissionIdsRequest {
    @JsonAlias({"permission_ids", "permissionIds"})
    private List<Long> permissionIds;
}
