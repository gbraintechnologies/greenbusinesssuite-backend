package com.mesh_suite.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {
    private String username;
    private String status;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String profileImage;
    private String tenantid;
}
