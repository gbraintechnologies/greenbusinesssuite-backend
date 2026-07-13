package com.mesh_suite.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mesh_suite.config.FlexibleStringDeserializer;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatusResponse {

    @JsonProperty("trans_ref")
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String transRef;

    @JsonProperty("trans_id")
    private String transId;

    @JsonProperty("trans_status")
    private String transStatus;

    @JsonProperty("message")
    private String message;
}
