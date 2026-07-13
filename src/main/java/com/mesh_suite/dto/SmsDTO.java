package com.mesh_suite.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsDTO {
    private String toNumber;
    private String message;
    private String from;

}