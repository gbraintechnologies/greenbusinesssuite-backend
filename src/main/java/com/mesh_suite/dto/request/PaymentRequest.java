package com.mesh_suite.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentRequest {

    @JsonProperty("customer_number")
    private String customerNumber;

    @JsonProperty("amount")
    private BigDecimal amount;

    private String exttrid;

    private String reference;

    private String nw;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty("trans_type")
    private String transType;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("service_id")
    private Integer serviceId;

    private String ts;

    private String nickname;
}