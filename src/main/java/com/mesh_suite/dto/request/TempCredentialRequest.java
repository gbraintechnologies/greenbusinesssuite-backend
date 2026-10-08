package com.mesh_suite.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class TempCredentialRequest {
    @JsonAlias({"user_id", "userId"})
    private Long userId;
    private String channel;
}
