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
import com.mesh_suite.service.notify.S3Service;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private S3Service s3Service;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public Long createCompanyBranding(CompanyBranding companyBranding) {

        companyBrandingRepository.findByTenancyId(companyBranding.getTenancyId())
                .ifPresent(existing -> {
                    throw new DuplicateKeyException("Branding already exists for this tenancy ID: " + companyBranding.getTenancyId());
                });


        companyBrandingRepository.findByCompanyId(companyBranding.getCompanyId())
                .ifPresent(existing -> {
                    throw new DuplicateKeyException("Branding already exists for this company ID: " + companyBranding.getCompanyId());
                });

        if (companyBranding.getSlug() != null && !companyBranding.getSlug().isBlank()) {
            if (companyBrandingRepository.existsBySlug(companyBranding.getSlug())) {
                throw new DuplicateKeyException("Branding already exists for slug: " + companyBranding.getSlug());
            }
        }


        if (companyBranding.getDomain() != null && !companyBranding.getDomain().isBlank()) {
            if (companyBrandingRepository.existsByDomain(companyBranding.getDomain())) {
                throw new DuplicateKeyException("Branding already exists for domain: " + companyBranding.getDomain());
            }
        }

        CompanyBranding savedCompanyBranding = companyBrandingRepository.save(companyBranding);
        return savedCompanyBranding.getId();
    }

    public CompanyBranding findById(Long id) {
        return companyBrandingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company branding not found with id: " + id));
    }

    public CompanyBranding findByCompanyId(Long companyId) {
        return companyBrandingRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company branding not found for company ID: " + companyId));
    }


    public Optional<CompanyBrandingDetailsDTO> findBySlug(String slug) {
        return companyBrandingRepository.findBySlug(slug).map(this::mapToDetailsDTO);
    }

    public Optional<CompanyBrandingDetailsDTO> findByDomain(String domain) {
        return companyBrandingRepository.findByDomain(domain).map(this::mapToDetailsDTO);
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


        if (companyBrandingDTO.getSlug() != null && !companyBrandingDTO.getSlug().isBlank()) {
            if (!companyBrandingDTO.getSlug().equals(companyBranding.getSlug())) {
                if (companyBrandingRepository.existsBySlug(companyBrandingDTO.getSlug())) {
                    throw new DuplicateKeyException("Slug already exists: " + companyBrandingDTO.getSlug());
                }
                companyBranding.setSlug(companyBrandingDTO.getSlug());
            }
        }


        if (companyBrandingDTO.getDomain() != null && !companyBrandingDTO.getDomain().isBlank()) {
            if (!companyBrandingDTO.getDomain().equals(companyBranding.getDomain())) {
                if (companyBrandingRepository.existsByDomain(companyBrandingDTO.getDomain())) {
                    throw new DuplicateKeyException("Domain already exists: " + companyBrandingDTO.getDomain());
                }
                companyBranding.setDomain(companyBrandingDTO.getDomain());
            }
        }

        companyBranding.setModuleIds(companyBrandingDTO.getModuleIds());
        companyBranding.setCategorySpecificModuleIds(companyBrandingDTO.getCategorySpecificModuleIds());

        return companyBrandingRepository.save(companyBranding);
    }

    // NEW: Helper method to map to DTO
    private CompanyBrandingDetailsDTO mapToDetailsDTO(CompanyBranding branding) {
        Set<CompanyBrandingDetailsDTO.ModuleDTO> modules = fetchModules(branding.getModuleIds());
        Set<CompanyBrandingDetailsDTO.CategorySpecificModuleDto> categorySpecificModules =
                fetchCategorySpecificModulesByIds(branding.getCategorySpecificModuleIds());

        return new CompanyBrandingDetailsDTO(
                branding.getId(),
                branding.getTenancyId(),
                branding.getCompanyId(),
                branding.getCompanyName(),
                branding.getLogo(),
                branding.getColor(),
                branding.getSlug(),
                branding.getDomain(),
                modules,
                categorySpecificModules
        );
    }

    public Optional<CompanyBrandingDetailsDTO> findByTenancyId(String tenancyId) {
        return companyBrandingRepository.findByTenancyId(tenancyId).map(this::mapToDetailsDTO);
    }

    public Optional<CompanyBrandingDetailsDTO> findDetailsByCompanyId(Long companyId) {
        return companyBrandingRepository.findByCompanyId(companyId).map(this::mapToDetailsDTO);
    }

    @Transactional
    public CompanyBranding uploadLogoByTenancyId(String tenancyId, MultipartFile file) {
        CompanyBranding branding = companyBrandingRepository.findByTenancyId(tenancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company branding not found for tenancyId: " + tenancyId));
        return uploadLogo(branding, file);
    }

    @Transactional
    public CompanyBranding uploadLogoByCompanyId(Long companyId, MultipartFile file) {
        CompanyBranding branding = findByCompanyId(companyId);
        return uploadLogo(branding, file);
    }

    @Transactional
    public CompanyBranding deleteLogoByTenancyId(String tenancyId) {
        CompanyBranding branding = companyBrandingRepository.findByTenancyId(tenancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company branding not found for tenancyId: " + tenancyId));
        return clearLogo(branding);
    }

    @Transactional
    public CompanyBranding deleteLogoByCompanyId(Long companyId) {
        CompanyBranding branding = findByCompanyId(companyId);
        return clearLogo(branding);
    }

    private CompanyBranding uploadLogo(CompanyBranding branding, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Logo file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/png")
                || contentType.equals("image/jpeg")
                || contentType.equals("image/jpg")
                || contentType.equals("image/avif")
                || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Unsupported logo format. Allowed: JPG, PNG, AVIF, WEBP");
        }

        String previousLogo = branding.getLogo();
        String logoUrl = s3Service.uploadFile(file);
        branding.setLogo(logoUrl);
        CompanyBranding saved = companyBrandingRepository.save(branding);

        if (previousLogo != null && !previousLogo.isBlank() && !previousLogo.equals(logoUrl)) {
            try {
                s3Service.deleteFile(previousLogo);
            } catch (Exception ignored) {
                // Keep branding update even if old object cleanup fails
            }
        }

        return saved;
    }

    private CompanyBranding clearLogo(CompanyBranding branding) {
        String previousLogo = branding.getLogo();
        branding.setLogo(null);
        CompanyBranding saved = companyBrandingRepository.save(branding);

        if (previousLogo != null && !previousLogo.isBlank()) {
            try {
                s3Service.deleteFile(previousLogo);
            } catch (Exception ignored) {
                // Branding logo cleared even if storage delete fails
            }
        }

        return saved;
    }

    public boolean deleteBrandingById(Long id) {
        if (companyBrandingRepository.existsById(id)) {
            companyBrandingRepository.findById(id).ifPresent(branding -> {
                if (branding.getLogo() != null && !branding.getLogo().isBlank()) {
                    try {
                        s3Service.deleteFile(branding.getLogo());
                    } catch (Exception ignored) {
                        // Continue with branding delete
                    }
                }
            });
            companyBrandingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Set<CompanyBrandingDetailsDTO.ModuleDTO> fetchModules(Set<Long> moduleIds) {
        if (moduleIds == null || moduleIds.isEmpty()) {
            return Collections.emptySet();
        }
        return moduleRepository.findAllById(moduleIds)
                .stream()
                .filter(Objects::nonNull)
                .map(module -> new CompanyBrandingDetailsDTO.ModuleDTO(
                        module.getId(),
                        module.getModuleName(),
                        module.getModuleDescription(),
                        module.getAdminFeatures(),
                        module.getClientFeatures()
                ))
                .collect(Collectors.toSet());
    }

    private Set<CompanyBrandingDetailsDTO.CategorySpecificModuleDto> fetchCategorySpecificModulesByIds(Set<Long> categorySpecificModuleIds) {
        if (categorySpecificModuleIds == null || categorySpecificModuleIds.isEmpty()) {
            return Collections.emptySet();
        }

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
        companyBrandingRepository.findByTenancyId(tenancyId).ifPresent(branding -> {
            if (branding.getLogo() != null && !branding.getLogo().isBlank()) {
                try {
                    s3Service.deleteFile(branding.getLogo());
                } catch (Exception ignored) {
                    // Continue with branding delete
                }
            }
        });
        companyBrandingRepository.deleteByTenancyId(tenancyId);
    }
}