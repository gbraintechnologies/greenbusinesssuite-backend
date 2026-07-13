package com.mesh_suite.dto;

import com.mesh_suite.constant.forms.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDataWithPaymentStatus {
    private FormDataProjection formData;
    private PaymentStatus paymentStatus;
}