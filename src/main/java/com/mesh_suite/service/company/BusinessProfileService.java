package com.mesh_suite.service.company;

import com.mesh_suite.constant.forms.Gender;
import com.mesh_suite.constant.forms.Sector;
import com.mesh_suite.constant.forms.TypeOfBusiness;
import com.mesh_suite.dao.company.BusinessProfileRepository;
import com.mesh_suite.domain.company.BusinessProfile;
import com.mesh_suite.dto.BusinessProfileDto;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.service.notify.S3Service;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final S3Service s3Service;
    @Autowired
    private  ModelMapper modelMapper;
    public BusinessProfileService(BusinessProfileRepository businessProfileRepository, S3Service s3Service) {
        this.businessProfileRepository = businessProfileRepository;
        this.s3Service = s3Service;
    }

    // Create new business profile
    @Transactional
    public Long createBusinessProfile(BusinessProfileDto dto) {
        BusinessProfile businessProfile = new BusinessProfile(dto);

        // Upload Business Owner ID Image to S3
        if (dto.getBusinessOwnerIdImage() != null && !dto.getBusinessOwnerIdImage().isEmpty()) {
            String businessOwnerIdImageUrl = s3Service.uploadFile(dto.getBusinessOwnerIdImage());
            businessProfile.setBusinessOwnerIdImage(businessOwnerIdImageUrl);
        }

        // Upload Business Document Image to S3
        if (dto.getBusinessDocumentImage() != null && !dto.getBusinessDocumentImage().isEmpty()) {
            String businessDocumentImageUrl = s3Service.uploadFile(dto.getBusinessDocumentImage());
            businessProfile.setBusinessDocumentImage(businessDocumentImageUrl);
        }
        // Validate fields and set isCompleted flag
        businessProfile.setCompleted(businessProfile.validateFields());
        // Save profile in the database
        return businessProfileRepository.save(businessProfile).getId();
    }

    // Get all business profiles
    public Paginate<BusinessProfile> getAllBusinessProfiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessProfile> allProfile = businessProfileRepository.findAll(pageable);

        return new Paginate<>(
                allProfile.getNumber(),
                allProfile.getSize(),
                allProfile.getTotalElements(),
                allProfile.getTotalPages(),
                allProfile.getContent()
        );
    }
    // Get business profile by ID
    public BusinessProfile getBusinessProfileById(Long id) {
        return businessProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found with id: " + id));
    }
    public List<BusinessProfile> getBusinessProfilesByUserId(Long userId) {
        return businessProfileRepository.findByUserId(userId);
    }
    // Update business profile
    public BusinessProfile updateBusinessProfile(BusinessProfile profile) {
        BusinessProfile existingProfile = businessProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Business profile with ID " + profile.getId() + " not found"));
        modelMapper.map(profile, existingProfile);
        existingProfile.setCompleted(profile.validateFields());
        return businessProfileRepository.save(existingProfile);
    }

    // Delete business profile
    public void deleteBusinessProfile(Long id) {
        BusinessProfile profile = getBusinessProfileById(id);
        businessProfileRepository.delete(profile);
    }
    public  Map<String, Long> getCountByGender() {
        Map<String, Long> genderStats = businessProfileRepository.countByGender().stream()
                .collect(Collectors.toMap(
                        result -> result[0] != null ? result[0].toString() : "",
                        result -> result[1] != null ? (Long) result[1] : 0L
                ));

        //  All Gender Enum values are included in the response
        Arrays.stream(Gender.values())
                .forEach(gender -> genderStats.putIfAbsent(gender.toString(), 0L));

        return genderStats;
    }
    public Map<String, Long> getCountByTypeOfBusiness() {
        Map<String, Long> businessStats = businessProfileRepository.countByTypeOfBusiness().stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (Long) result[1]
                ));
        businessStats.remove("");
        Arrays.stream(TypeOfBusiness.values())
                .forEach(type -> businessStats.putIfAbsent(type.toString(), 0L));

        return businessStats;
    }

    public Map<String, Long> getCountBySector() {
        Map<String, Long> sectorStats = businessProfileRepository.countBySector().stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (Long) result[1]
                ));

        sectorStats.remove("");
        Arrays.stream(Sector.values())
                .forEach(sector -> sectorStats.putIfAbsent(sector.toString(), 0L));

        return sectorStats;
    }
}
