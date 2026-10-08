package com.mesh_suite.service.company;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.constant.forms.CompanyCurrency;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.domain.user.CustomProfileOwnerType;
import com.mesh_suite.dto.request.LegacyCompanyWriteRequest;
import com.mesh_suite.dto.response.CompanyResponseDTO;
import com.mesh_suite.exception.BadRequestException;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.mapper.UserCompanyMapper;
import com.mesh_suite.service.user.CustomProfileService;
import com.mesh_suite.util.LegacyMaps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompanyCompatibilityService {

    private static final long DESCRIPTION_ITEM_ID = 1L;

    private final UserCompanyRepository userCompanyRepository;
    private final CustomProfileService customProfileService;

    @Transactional
    public CompanyResponseDTO editWithCustomFields(Long companyId, LegacyCompanyWriteRequest request) {
        if (companyId == null) {
            throw new BadRequestException("Company id is required");
        }
        UserCompany company = userCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        Map<String, Object> data = request == null ? null : request.getCompanyData();
        applyCompanyData(company, data);

        String description = customProfileService.valueFor(
                request == null ? null : request.getCustomFields(),
                DESCRIPTION_ITEM_ID
        );
        if (description != null) {
            company.setDescription(description);
        }

        userCompanyRepository.save(company);
        customProfileService.replace(
                CustomProfileOwnerType.COMPANY,
                company.getId(),
                request == null ? null : request.getCustomFields()
        );
        return UserCompanyMapper.toResponseDto(company);
    }

    private void applyCompanyData(UserCompany company, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        String name = LegacyMaps.text(data, "companyName", "company_name");
        if (name != null) {
            company.setCompanyName(name);
        }
        String description = LegacyMaps.text(data, "description");
        if (description != null) {
            company.setDescription(description);
        }
        String contactName = LegacyMaps.text(data, "primaryContactName", "primary_contact_name");
        if (contactName != null) {
            company.setPrimaryContactName(contactName);
        }
        String contactEmail = LegacyMaps.text(data, "primaryContactEmail", "primary_contact_email");
        if (contactEmail != null) {
            company.setPrimaryContactEmail(contactEmail);
        }
        String contactPhone = LegacyMaps.text(data, "primaryContactPhoneNumber", "primary_contact_phone_number");
        if (contactPhone != null) {
            company.setPrimaryContactPhoneNumber(contactPhone);
        }
        String logo = LegacyMaps.text(data, "companyLogo", "company_logo");
        if (logo != null) {
            company.setCompanyLogo(logo);
        }
        String address = LegacyMaps.text(data, "companyAddress", "company_address");
        if (address != null) {
            company.setCompanyAddress(address);
        }
        String industry = LegacyMaps.text(data, "industry");
        if (industry != null) {
            company.setIndustry(industry);
        }
        String code = LegacyMaps.text(data, "companyCode", "company_code");
        if (code != null) {
            company.setCompanyCode(code);
        }
        String senderId = LegacyMaps.text(data, "company_sms_sender_id", "smsSenderId", "sms_sender_id");
        if (senderId != null) {
            company.setSmsSenderId(senderId);
        }
        String status = LegacyMaps.text(data, "status");
        if (status != null) {
            try {
                company.setStatus(CompanyStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Unknown company status: " + status);
            }
        }
        String currency = LegacyMaps.text(data, "primaryCurrency", "primary_currency");
        if (currency != null) {
            try {
                company.setPrimaryCurrency(CompanyCurrency.valueOf(currency.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Unknown currency: " + currency);
            }
        }
    }
}
