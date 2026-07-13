package com.mesh_suite.dto.request;

public record AuthRequestDTO(
    String usernameOrPhoneNumber,
    String password
) {}

