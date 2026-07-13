package com.mesh_suite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentResponse {

    @JsonProperty("resp_code")
    private String respCode;

    @JsonProperty("resp_desc")
    private String respDesc;
}
