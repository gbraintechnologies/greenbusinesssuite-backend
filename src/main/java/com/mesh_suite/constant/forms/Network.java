package com.mesh_suite.constant.forms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Network {

    AIR("AIR"),
    TIG("TIG"),
    VOD("VOD"),
    MTN("MTN"),
    MAS("MAS"),
    BNK("BNK"),
    VIS("VIS");

    private final String value;

    Network(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Network fromValue(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Network is required");
        }

        return Arrays.stream(Network.values())
                .filter(n -> n.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid network: " + value));
    }
}