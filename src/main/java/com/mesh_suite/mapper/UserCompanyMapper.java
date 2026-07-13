package com.mesh_suite.mapper;

import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.CompanyRegResp;
import com.mesh_suite.dto.request.CompanyCreateDTO;
import com.mesh_suite.dto.request.CompanyUpdateDTO;
import com.mesh_suite.dto.response.CompanyResponseDTO;

public class UserCompanyMapper {

    public static UserCompany toEntity(CompanyCreateDTO dto, Users companyAdmin) {
        if (dto == null) {
            return null;
        }
        return UserCompany.builder()
                .companyName(dto.getCompanyName())
                .status(dto.getStatus())
                .description(dto.getDescription())
                .primaryContactName(dto.getPrimaryContactName())
                .primaryContactEmail(dto.getPrimaryContactEmail())
                .primaryContactPhoneNumber(dto.getPrimaryContactPhoneNumber())
                .companyLogo(dto.getCompanyLogo())
                .companyAddress(dto.getCompanyAddress())
                .companyDigitalAddress(dto.getCompanyDigitalAddress())
                .industry(dto.getIndustry())
                .companyMerchantMomoNumber(dto.getCompanyMerchantMomoNumber())
                .companyBankName(dto.getCompanyBankName())
                .taxId(dto.getTaxId())
                .startOfDayTime(dto.getStartOfDayTime())
                .endOfDayTime(dto.getEndOfDayTime())
                .primaryCurrency(dto.getPrimaryCurrency())
                .secondaryCurrency(dto.getSecondaryCurrency())
                .companyAdmin(companyAdmin)
                .companyCode(dto.getCompanyCode())
                .buildStatus(dto.getBuildStatus())
                .driverName(dto.getDriverName())
                .dbUrl(dto.getDbUrl())
                .assignedFormIds(dto.getAssignedFormIds())
                .createdOn(java.time.ZonedDateTime.now())
                .build();
    }

    public static void updateEntityFromDto(CompanyUpdateDTO dto, UserCompany entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getCompanyName() != null) {
            entity.setCompanyName(dto.getCompanyName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getPrimaryContactPhoneNumber() != null) {
            entity.setPrimaryContactPhoneNumber(dto.getPrimaryContactPhoneNumber());
        }
        if (dto.getCompanyLogo() != null) {
            entity.setCompanyLogo(dto.getCompanyLogo());
        }
        if (dto.getCompanyAddress() != null) {
            entity.setCompanyAddress(dto.getCompanyAddress());
        }
        if (dto.getCompanyDigitalAddress() != null) {
            entity.setCompanyDigitalAddress(dto.getCompanyDigitalAddress());
        }
        if (dto.getIndustry() != null) {
            entity.setIndustry(dto.getIndustry());
        }
        if (dto.getCompanyMerchantMomoNumber() != null) {
            entity.setCompanyMerchantMomoNumber(dto.getCompanyMerchantMomoNumber());
        }
        if (dto.getCompanyBankName() != null) {
            entity.setCompanyBankName(dto.getCompanyBankName());
        }
        if (dto.getTaxId() != null) {
            entity.setTaxId(dto.getTaxId());
        }
        if (dto.getPrimaryCurrency() != null) {
            entity.setPrimaryCurrency(dto.getPrimaryCurrency());
        }
        if (dto.getSecondaryCurrency() != null) {
            entity.setSecondaryCurrency(dto.getSecondaryCurrency());
        }
        if (dto.getCompanyAdminId() != null) {
            Users companyAdmin = new Users();
            companyAdmin.setId(dto.getCompanyAdminId());
            entity.setCompanyAdmin(companyAdmin);
        }
        if (dto.getDriverName() != null) {
            entity.setDriverName(dto.getDriverName());
        }
        if (dto.getDbUrl() != null) {
            entity.setDbUrl(dto.getDbUrl());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getPrimaryContactName() != null) {
            entity.setPrimaryContactName(dto.getPrimaryContactName());
        }
        if (dto.getPrimaryContactEmail() != null) {
            entity.setPrimaryContactEmail(dto.getPrimaryContactEmail());
        }
        if (dto.getCompanyCode() != null) {
            entity.setCompanyCode(dto.getCompanyCode());
        }
        if (dto.getAssignedFormIds() != null) {
            entity.setAssignedFormIds(dto.getAssignedFormIds());
        }
        if (dto.getStartOfDayTime() != null) {
            entity.setStartOfDayTime(dto.getStartOfDayTime());
        }
        if (dto.getEndOfDayTime() != null) {
            entity.setEndOfDayTime(dto.getEndOfDayTime());
        }
        entity.setUpdatedOn(java.time.ZonedDateTime.now());
        
    }

    public static CompanyUpdateDTO toUpdateDto(UserCompany entity) {
        if (entity == null) {
            return null;
        }
        return CompanyUpdateDTO.builder()
                .id(entity.getId() != null ? entity.getId().longValue() : null)
                .companyCode(entity.getCompanyCode())
                .description(entity.getDescription())
                .primaryContactPhoneNumber(entity.getPrimaryContactPhoneNumber())
                .companyLogo(entity.getCompanyLogo())
                .companyAddress(entity.getCompanyAddress())
                .companyDigitalAddress(entity.getCompanyDigitalAddress())
                .industry(entity.getIndustry())
                .companyMerchantMomoNumber(entity.getCompanyMerchantMomoNumber())
                .companyBankName(entity.getCompanyBankName())
                .taxId(entity.getTaxId())
                .startOfDayTime(entity.getStartOfDayTime())
                .endOfDayTime(entity.getEndOfDayTime())
                .primaryCurrency(entity.getPrimaryCurrency())
                .secondaryCurrency(entity.getSecondaryCurrency())
                .companyAdminId(entity.getCompanyAdmin() != null ? entity.getCompanyAdmin().getId() : null)
                .driverName(entity.getDriverName())
                .dbUrl(entity.getDbUrl())
                .companyName(entity.getCompanyName())
                .status(entity.getStatus())
                .primaryContactName(entity.getPrimaryContactName())
                .primaryContactEmail(entity.getPrimaryContactEmail())
                .companyCode(entity.getCompanyCode())
                .assignedFormIds(entity.getAssignedFormIds())
                .build();
    }

    public static CompanyRegResp toRegResp(UserCompany entity, String message) {
        if (entity == null) {
            return null;
        }
        return CompanyRegResp.builder()
                .id(entity.getId() != null ? entity.getId().longValue() : null)
                .companyName(entity.getCompanyName())
                .status(entity.getStatus())
                .primaryContactName(entity.getPrimaryContactName())
                .primaryContactEmail(entity.getPrimaryContactEmail())
                .companyCode(entity.getCompanyCode())
                .assignedFormIds(entity.getAssignedFormIds())
                .message(message)
                .build();
    }

    public static CompanyResponseDTO toResponseDto(UserCompany entity) {
        if (entity == null) {
            return null;
        }
        return CompanyResponseDTO.builder()
                .id(entity.getId() != null ? entity.getId().longValue() : null)
                .companyName(entity.getCompanyName())
                .description(entity.getDescription())
                .primaryContactPhoneNumber(entity.getPrimaryContactPhoneNumber())
                .companyLogo(entity.getCompanyLogo())
                .companyAddress(entity.getCompanyAddress())
                .companyDigitalAddress(entity.getCompanyDigitalAddress())
                .industry(entity.getIndustry())
                .companyMerchantMomoNumber(entity.getCompanyMerchantMomoNumber())
                .companyBankName(entity.getCompanyBankName())
                .taxId(entity.getTaxId())
                .startOfDayTime(entity.getStartOfDayTime())
                .endOfDayTime(entity.getEndOfDayTime())
                .primaryCurrency(entity.getPrimaryCurrency())
                .secondaryCurrency(entity.getSecondaryCurrency())
                .companyAdminId(entity.getCompanyAdmin() != null ? entity.getCompanyAdmin().getId() : null)
                .companyIdentifier(entity.getCompanyIdentifier())
                .driverName(entity.getDriverName())
                .dbUrl(entity.getDbUrl())
                .status(entity.getStatus())
                .buildStatus(entity.getBuildStatus())
                .primaryContactName(entity.getPrimaryContactName())
                .primaryContactEmail(entity.getPrimaryContactEmail())
                .companyCode(entity.getCompanyCode())
                .assignedFormIds(entity.getAssignedFormIds())
                .build();
    }
}
