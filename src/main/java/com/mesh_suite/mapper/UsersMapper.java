package com.mesh_suite.mapper;

import org.springframework.stereotype.Component;

import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.response.UserResponse;

@Component
public class UsersMapper {

    public UserResponse toUserResponse(Users user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhoneNumber())
                .createdAt(user.getCreatedOn())
                .updatedAt(user.getUpdatedOn())
                .status(user.getStatus())
                .companyIdentifier(user.getCompanyIdentifier())
                .build();
    }
}

