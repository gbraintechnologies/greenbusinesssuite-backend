package com.mesh_suite.controller.company;

import com.mesh_suite.domain.company.CompanyBranding;
import com.mesh_suite.dto.CompanyBrandingDTO;
import com.mesh_suite.dto.CompanyBrandingDetailsDTO;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.exception.DuplicateKeyException;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.service.company.CompanyBrandingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/mesh-suite/v1.0/company-branding", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
@Tag(name = "Company Branding Setup Config", description = "Setting up Company Branding")
@Slf4j
public class CompanyBrandingController {

    @Autowired
    private CompanyBrandingService companyBrandingService;

    @Autowired
    private ModelMapper modelMapper;

    // ========== CREATE ==========
    @PostMapping("/create")
    @Operation(summary = "Create a new Company Brand")
    public ResponseEntity<?> createCompanyBranding(@RequestBody CompanyBranding companyBrand) {
        try {
            Long companyBrandId = companyBrandingService.createCompanyBranding(companyBrand);
            return ResponseEntity.status(HttpStatus.CREATED).body(companyBrandId);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ========== GET BY ID ==========
    @GetMapping("/{id}")
    @Operation(summary = "Retrieve Company branding setup by ID")
    public ResponseEntity<CompanyBranding> findCompanyBrandingById(@PathVariable Long id) {
        CompanyBranding companyBranding = companyBrandingService.findById(id);
        return ResponseEntity.ok(companyBranding);
    }

    // ========== GET BY SLUG ==========
    @GetMapping("/find-by-slug/{slug}")
    @Operation(summary = "Find Company Branding Details by Slug",
            description = "Fetches the company branding details by slug (URL-friendly company name)")
    public ResponseEntity<CompanyBrandingDetailsDTO> findBySlug(@PathVariable String slug) {
        return companyBrandingService.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== GET BY DOMAIN ==========
    @GetMapping("/find-by-domain/{domain}")
    @Operation(summary = "Find Company Branding Details by Domain",
            description = "Fetches the company branding details by custom domain")
    public ResponseEntity<CompanyBrandingDetailsDTO> findByDomain(@PathVariable String domain) {
        return companyBrandingService.findByDomain(domain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== CHECK SLUG AVAILABILITY ==========
    @GetMapping("/check-slug-availability/{slug}")
    @Operation(summary = "Check if slug is available",
            description = "Checks if a slug is available for use")
    public ResponseEntity<Boolean> isSlugAvailable(@PathVariable String slug) {
        return ResponseEntity.ok(!companyBrandingService.findBySlug(slug).isPresent());
    }

    // ========== CHECK DOMAIN AVAILABILITY ==========
    @GetMapping("/check-domain-availability/{domain}")
    @Operation(summary = "Check if domain is available",
            description = "Checks if a domain is available for use")
    public ResponseEntity<Boolean> isDomainAvailable(@PathVariable String domain) {
        return ResponseEntity.ok(!companyBrandingService.findByDomain(domain).isPresent());
    }

    // ========== GET BY TENANCY ID ==========
    @GetMapping("/find-by-tenancy-id/{tenancyId}")
    @Operation(summary = "Find Company Branding Details by Tenancy ID",
            description = "Fetches the company branding details, including associated modules and categories, by tenancy ID.")
    public ResponseEntity<CompanyBrandingDetailsDTO> findByTenancyId(@PathVariable String tenancyId) {
        return companyBrandingService.findByTenancyId(tenancyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== GET BY COMPANY ID ==========
    @GetMapping("/find-by-company-id/{companyId}")
    @Operation(summary = "Find Company Branding Details by Company ID",
            description = "Fetches the company branding details for a created company by company ID.")
    public ResponseEntity<CompanyBrandingDetailsDTO> findByCompanyId(@PathVariable Long companyId) {
        return companyBrandingService.findDetailsByCompanyId(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== UPDATE ==========
    @PutMapping("/update")
    @Operation(summary = "Update a Company Branding")
    public ResponseEntity<?> updateCompanyBranding(@RequestBody CompanyBrandingDTO companyBrandingDTO) {
        try {
            CompanyBranding branding = companyBrandingService.updateCompanyBranding(companyBrandingDTO);
            CompanyBrandingDTO responseDTO = modelMapper.map(branding, CompanyBrandingDTO.class);
            return ResponseEntity.ok(responseDTO);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ========== UPLOAD LOGO BY TENANCY ID ==========
    @PostMapping(value = "/tenancy/{tenancyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload company branding logo by tenancy ID")
    public ResponseEntity<?> uploadLogoByTenancyId(
            @PathVariable String tenancyId,
            @RequestPart("file") MultipartFile file) {
        try {
            CompanyBranding branding = companyBrandingService.uploadLogoByTenancyId(tenancyId, file);
            return ResponseEntity.ok(branding);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ========== UPLOAD LOGO BY COMPANY ID ==========
    @PostMapping(value = "/company/{companyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload company branding logo by company ID")
    public ResponseEntity<?> uploadLogoByCompanyId(
            @PathVariable Long companyId,
            @RequestPart("file") MultipartFile file) {
        try {
            CompanyBranding branding = companyBrandingService.uploadLogoByCompanyId(companyId, file);
            return ResponseEntity.ok(branding);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ========== DELETE LOGO BY TENANCY ID ==========
    @DeleteMapping("/tenancy/{tenancyId}/logo")
    @Operation(summary = "Delete company branding logo by tenancy ID")
    public ResponseEntity<?> deleteLogoByTenancyId(@PathVariable String tenancyId) {
        try {
            CompanyBranding branding = companyBrandingService.deleteLogoByTenancyId(tenancyId);
            return ResponseEntity.ok(branding);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ========== DELETE LOGO BY COMPANY ID ==========
    @DeleteMapping("/company/{companyId}/logo")
    @Operation(summary = "Delete company branding logo by company ID")
    public ResponseEntity<?> deleteLogoByCompanyId(@PathVariable Long companyId) {
        try {
            CompanyBranding branding = companyBrandingService.deleteLogoByCompanyId(companyId);
            return ResponseEntity.ok(branding);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ========== DELETE BRANDING BY ID ==========
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete Company Branding by ID",
            description = "Deletes the company branding by its ID.")
    public ResponseEntity<Void> deleteBrandingById(@PathVariable Long id) {
        boolean deleted = companyBrandingService.deleteBrandingById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ========== GET ALL WITH PAGINATION ==========
    @GetMapping("/all/{page}/{size}")
    @Operation(summary = "Retrieve all Company branding with pagination")
    public ResponseEntity<Paginate<CompanyBranding>> getAllBusinessProfiles(
            @PathVariable int page,
            @PathVariable int size) {
        Paginate<CompanyBranding> branding = companyBrandingService.getAllCompanyBranding(page, size);
        return ResponseEntity.ok(branding);
    }

    // ========== DELETE BY TENANCY ID ==========
    @DeleteMapping("/tenant/{tenantId}")
    @Operation(summary = "Delete Company branding by tenantId")
    public ResponseEntity<Void> deleteByTenancyId(@PathVariable String tenantId) {
        companyBrandingService.deleteByTenancyId(tenantId);
        return ResponseEntity.noContent().build();
    }
}