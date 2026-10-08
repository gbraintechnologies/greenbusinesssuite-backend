package com.mesh_suite.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mesh_suite.constant.forms.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private UserStatus status;
    private String companyIdentifier;
    private String username;
    private String roleName;
    private Long roleId;
    private String profileImage;
    @Builder.Default
    private List<CustomFieldValueView> customProfileValues = new ArrayList<>();
    @Builder.Default
    private List<UserProfileView> profiles = new ArrayList<>();

    @JsonProperty("first_name")
    public String getFirst_name() {
        return firstName;
    }

    @JsonProperty("last_name")
    public String getLast_name() {
        return lastName;
    }

    @JsonProperty("phone_number")
    public String getPhone_number() {
        return phone;
    }

    @JsonProperty("mobile_phone_number")
    public String getMobile_phone_number() {
        return phone;
    }

    @JsonProperty("user_status")
    public String getUser_status() {
        return status == null ? null : status.name();
    }

    @JsonProperty("company_identifier")
    public String getCompany_identifier() {
        return companyIdentifier;
    }

    @JsonProperty("profile_image")
    public String getProfile_image() {
        return profileImage;
    }

    @JsonProperty("role_name")
    public String getRole_name() {
        return roleName;
    }

    @JsonProperty("role_id")
    public Long getRole_id() {
        return roleId;
    }

    @JsonProperty("custom_profile_values")
    public List<CustomFieldValueView> getCustom_profile_values() {
        return customProfileValues == null ? List.of() : customProfileValues;
    }
}
