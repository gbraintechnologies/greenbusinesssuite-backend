package com.mesh_suite.dto;

import lombok.Data;

@Data
public class DeactivationResponse {
    private boolean success;
    private String message;

    public DeactivationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

}
