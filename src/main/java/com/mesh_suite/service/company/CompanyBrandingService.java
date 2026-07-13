package com.mesh_suite.service.company;

import com.mesh_suite.dao.company.CategorySpecificModuleRepository;
import com.mesh_suite.dao.company.CompanyBrandingRepository;
import com.mesh_suite.dao.company.ModuleRepository;
import com.mesh_suite.domain.company.CompanyBranding;
import com.mesh_suite.dto.CompanyBrandingDTO;
import com.mesh_suite.dto.CompanyBrandingDetailsDTO;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.exception.DuplicateKeyException;
import com.mesh_suite.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompanyBrandingService {
    @Autowired
    private CompanyBrandingRepository companyBrandingRepository;
    @Autowired
    private ModuleRepository moduleRepository;
    @Autowired
    private CategorySpecificModuleRepository categorySpecificModuleRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Transactional
    public Long createCompanyBranding(CompanyBranding companyBranding) {
        companyBrandingRepository.findByTenancyId(companyBranding.getTenancyId())
                .ifPresent(existing -> {
                    throw new DuplicateKeyException("Branding already exists for this tenancy ID: " + companyBranding.getTenancyId());
                });

        Optional<CompanyBranding> existingByCompany = companyBrandingRepository.findById(companyBranding.getCompanyId());
        if (existingByCompany.isPresent()) {
            throw new DuplicateKeyException("Branding already exists for this company ID: " + companyBranding.getCompanyId());
        }

        CompanyBranding savedCompanyBranding = companyBrandingRepository.save(companyBranding);
        return savedCompanyBranding.getId();
    }

    public CompanyBranding findById(Long id) {
        return companyBrandingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company branding not found with id: " + id));
    }
    public CompanyBranding updateCompanyBranding(CompanyBrandingDTO companyBrandingDTO) {
        Optional<CompanyBranding> optionalBranding = companyBrandingRepository.findByTenancyId(companyBrandingDTO.getTenancyId());

        if (!optionalBranding.isPresent()) {
            throw new ResourceNotFoundException("CompanyBranding not found for tenancyId: " + companyBrandingDTO.getTenancyId());
        }

        CompanyBranding companyBranding = optionalBranding.get();
        companyBranding.setCompanyId(companyBrandingDTO.getCompanyId());
        companyBranding.setCompanyName(companyBrandingDTO.getCompanyName());
        companyBranding.setLogo(companyBrandingDTO.getLogo());
        companyBranding.setColor(companyBrandingDTO.getColor());
        companyBranding.setModuleIds(companyBrandingDTO.getModuleIds());
        companyBranding.setCategorySpecificModuleIds(companyBrandingDTO.getCategorySpecificModuleIds());

        return companyBrandingRepository.save(companyBranding);
    }
    public Optional<CompanyBrandingDetailsDTO> findByTenancyId(String tenancyId) {
        return companyBrandingRepository.findByTenancyId(tenancyId).map(branding -> {

            // Fetch the modules based on the moduleIds present in the branding
            Set<CompanyBrandingDetailsDTO.ModuleDTO> modules = fetchModules(branding.getModuleIds());

            // Fetch and filter category-specific modules by the IDs present in branding
            Set<CompanyBrandingDetailsDTO.CategorySpecificModuleDto> categorySpecificModules = fetchCategorySpecificModulesByIds(branding.getCategorySpecificModuleIds());

            return new CompanyBrandingDetailsDTO(
                    branding.getId(),
                    branding.getTenancyId(),
                    branding.getCompanyId(),
                    branding.getCompanyName(),
                    branding.getLogo(),
                    branding.getColor(),
                    modules,
                    categorySpecificModules
            );
        });
    }

    public boolean deleteBrandingById(Long id) {
        if (companyBrandingRepository.existsById(id)) {
            companyBrandingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Set<CompanyBrandingDetailsDTO.ModuleDTO> fetchModules(Set<Long> moduleIds) {
        return moduleRepository.findAllById(moduleIds)
                .stream()
                .filter(Objects::nonNull)  // Filter out null values
                .map(module -> new CompanyBrandingDetailsDTO.ModuleDTO(
                        module.getId(),
                        module.getModuleName(),
                        module.getModuleDescription(),
                        module.getAdminFeatures(),
                        module.getClientFeatures()
                ))
                .collect(Collectors.toSet());
    }


    private Set<CompanyBrandingDetailsDTO.CategorySpecificModuleDto> fetchCategories(Set<Long> categoryIds) {
        return categorySpecificModuleRepository.findAllById(categoryIds) // Fetch by category IDs
                .stream()
                .map(module -> new CompanyBrandingDetailsDTO.CategorySpecificModuleDto(
                        module.getId(),
                        module.getModuleName(),
                        module.getAdminFeatures(),
                        module.getClientFeatures(),
                        module.isTemplate()
                ))
                .collect(Collectors.toSet());
    }

    private Set<CompanyBrandingDetailsDTO.CategorySpecificModuleDto> fetchCategorySpecificModulesByIds(Set<Long> categorySpecificModuleIds) {
        if (categorySpecificModuleIds == null || categorySpecificModuleIds.isEmpty()) {
            return Collections.emptySet();
        }

        // Fetch the entities matching the provided IDs and map to DTOs
        return categorySpecificModuleRepository.findAllById(categorySpecificModuleIds)
                .stream()
                .map(category -> new CompanyBrandingDetailsDTO.CategorySpecificModuleDto(
                        category.getId(),
                        category.getModuleName(),
                        category.getAdminFeatures(),
                        category.getClientFeatures(),
                        category.isTemplate()
                ))
                .collect(Collectors.toSet());
    }

    public Paginate<CompanyBranding> getAllCompanyBranding(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CompanyBranding> allProfile = companyBrandingRepository.findAll(pageable);

        return new Paginate<>(
                allProfile.getNumber(),
                allProfile.getSize(),
                allProfile.getTotalElements(),
                allProfile.getTotalPages(),
                allProfile.getContent()
        );
    }
    @Transactional
    public void deleteByTenancyId(String tenancyId) {
        companyBrandingRepository.deleteByTenancyId(tenancyId);
    }
}

