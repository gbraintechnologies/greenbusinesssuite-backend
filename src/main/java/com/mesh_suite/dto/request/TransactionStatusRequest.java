package com.mesh_suite.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionStatusRequest {

    @JsonProperty("exttrid")
    private String exttrid;

    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("trans_type")
    private String transType = "TSC";
}