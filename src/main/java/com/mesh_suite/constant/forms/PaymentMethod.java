package com.mesh_suite.constant.forms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PaymentMethod {

    MOBILE_MONEY("MOBILE_MONEY"),
    CREDIT_DEBIT_CARD("CREDIT_DEBIT_CARD"),
    BANK_TRANSFER("BANK_TRANSFER");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentMethod fromValue(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        return Arrays.stream(PaymentMethod.values())
                .filter(m -> m.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid payment method: " + value));
    }
}