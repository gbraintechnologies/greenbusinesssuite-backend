package com.mesh_suite.dto;

import com.mesh_suite.constant.forms.Network;
import com.mesh_suite.constant.forms.PaymentMethod;
import com.mesh_suite.constant.forms.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long billId;
    private Long responseId;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transRef;
    private String serviceName;
    private String customerEmail;
    private String customerName;
    private String phoneNumber;
    private String bankCode;
    @NotNull(message = "Network is required")
    private Network network;
    private LocalDateTime datePaid;
    private LocalDateTime updatedOn;
}
