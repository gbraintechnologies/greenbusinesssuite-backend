package com.mesh_suite.domain.company;

import com.mesh_suite.constant.forms.Gender;
import com.mesh_suite.constant.forms.Sector;
import com.mesh_suite.constant.forms.TypeOfBusiness;
import com.mesh_suite.dto.BusinessProfileDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "user_id")
    private Long userId;
    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_owner_name")
    private String businessOwnerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;
    @Column(name = "business_owner_Id_Image")
    private String businessOwnerIdImage;
    @Column(name = "business_document_image")
    private String businessDocumentImage;
    @Enumerated(EnumType.STRING)
    @Column(name = "sector")
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_of_business")
    private TypeOfBusiness typeOfBusiness;
    @Column(name = "business_registration_no")
   private String businessRegistrationNo;

    @Column(name = "business_address")
    private String businessAddress;

    @Column(name = "email")
    private String email;

   @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "tin")
    private String tin;
    @Column(name = "social_media_link")
    private String socialMediaLink;

    @CreationTimestamp
    @Column(name = "created_on" )
    private LocalDateTime createdOn;
    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
    @Column(name = "is_completed")
    private boolean isCompleted;
    public BusinessProfile(BusinessProfileDto dto) {
        this.companyId = dto.getCompanyId();
        this.userId = dto.getUserId();
        this.businessName = dto.getBusinessName();
        this.businessOwnerName = dto.getBusinessOwnerName();
        this.gender = dto.getGender();
        this.sector = dto.getSector();
        this.typeOfBusiness = dto.getTypeOfBusiness();
        this.businessRegistrationNo = dto.getBusinessRegistrationNo();
        this.businessAddress = dto.getBusinessAddress();
        this.email = dto.getEmail();
        this.phoneNumber = dto.getPhoneNumber();
        this.tin = dto.getTin();
        this.socialMediaLink = dto.getSocialMediaLink();
        this.isCompleted = dto.isCompleted();
    }
    public boolean validateFields() {
        return companyId != null &&
                userId != null &&
                businessName != null && !businessName.isEmpty() &&
                businessOwnerName != null && !businessOwnerName.isEmpty() &&
                gender != null &&
                sector != null &&
                typeOfBusiness != null &&
                businessRegistrationNo != null && !businessRegistrationNo.isEmpty() &&
                businessAddress != null && !businessAddress.isEmpty() &&
                email != null && !email.isEmpty() &&
                phoneNumber != null && !phoneNumber.isEmpty() &&
                tin != null && !tin.isEmpty() &&
                socialMediaLink != null && !socialMediaLink.isEmpty() &&
                businessOwnerIdImage != null && !businessOwnerIdImage.isEmpty() &&
                businessDocumentImage != null && !businessDocumentImage.isEmpty();
    }
}
