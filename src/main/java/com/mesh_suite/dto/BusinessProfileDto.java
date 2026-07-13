package com.mesh_suite.dto;

import com.mesh_suite.constant.forms.Gender;
import com.mesh_suite.constant.forms.Sector;
import com.mesh_suite.constant.forms.TypeOfBusiness;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfileDto {
    @NotNull
    private Long companyId;

    @NotNull
    private Long userId;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business owner name is required")
    private String businessOwnerName;

    private Gender gender;

    private MultipartFile businessOwnerIdImage;

    private MultipartFile businessDocumentImage;

    @NotNull(message = "Sector is required")
    private Sector sector;

    @NotNull(message = "Type of business is required")
    private TypeOfBusiness typeOfBusiness;

    @NotBlank(message = "Business registration number is required")
    private String businessRegistrationNo;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    private String tin;

    private String socialMediaLink;
    private boolean isCompleted;
}
