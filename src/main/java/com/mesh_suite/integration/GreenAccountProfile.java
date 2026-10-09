package com.mesh_suite.integration;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GreenAccountProfile {
    private final String greenAccountId;
    private final String displayName;
    private final String email;
    private final String phone;
    private final String ghanaCard;
}
