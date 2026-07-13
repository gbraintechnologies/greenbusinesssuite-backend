package com.mesh_suite.constant.forms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultDto {
    private Long paymentId;
    private PaymentStatus status;
    private String responseCode;
    private String responseDescription;
    private String transRef;
    private PaymentMethod paymentMethod;
}