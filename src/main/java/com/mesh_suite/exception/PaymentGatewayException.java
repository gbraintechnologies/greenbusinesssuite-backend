package com.mesh_suite.exception;

import com.mesh_suite.util.PaymentGatewayClient;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message){
        super(message);
    }
}
