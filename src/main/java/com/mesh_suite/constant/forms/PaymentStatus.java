package com.mesh_suite.constant.forms;

/*
public enum PaymentStatus {
    PENDING,
    SUCCESSFUL,
    FAILED,
    CANCELLED,
    REFUNDED

}
*/

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PaymentStatus {

    PENDING("PENDING"),
    SUCCESSFUL("SUCCESSFUL"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED"),
    REFUNDED("REFUNDED"),
    UNKNOWN("UNKNOWN");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        if (value == null) return UNKNOWN;

        return Arrays.stream(PaymentStatus.values())
                .filter(s -> s.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
