package com.mesh_suite.controller.company;

import com.mesh_suite.domain.company.CompanyBranding;
import com.mesh_suite.dto.CompanyBrandingDTO;
import com.mesh_suite.dto.CompanyBrandingDetailsDTO;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.exception.DuplicateKeyException;
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


    @GetMapping("/{id}")
    @Operation(summary = "Retrieve Company branding setup by ID")
    public ResponseEntity<CompanyBranding> findCompanyBrandingById(@PathVariable Long id) {
        CompanyBranding companyBranding = companyBrandingService.findById(id);
        return ResponseEntity.ok(companyBranding);
    }

    @PutMapping("/update")
    @Operation(summary = "Update a Company Branding")
    public ResponseEntity<CompanyBrandingDTO> updateCompanyBranding(@RequestBody CompanyBrandingDTO companyBrandingDTO) {

        CompanyBranding branding = companyBrandingService.updateCompanyBranding(companyBrandingDTO);
        CompanyBrandingDTO responseDTO = modelMapper.map(branding, CompanyBrandingDTO.class);
        return ResponseEntity.ok(responseDTO);
    }


    @Operation(summary = "Find Company Branding Details by Tenancy ID",
            description = "Fetches the company branding details, including associated modules and categories, by tenancy ID.")
    @GetMapping("/find-by-tenancy-id/{tenancyId}")
    public ResponseEntity<CompanyBrandingDetailsDTO> findByTenancyId(@PathVariable String tenancyId) {
        return companyBrandingService.findByTenancyId(tenancyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Find Company Branding Details by Company ID",
            description = "Fetches the company branding details for a created company by company ID.")
    @GetMapping("/find-by-company-id/{companyId}")
    public ResponseEntity<CompanyBrandingDetailsDTO> findByCompanyId(@PathVariable Long companyId) {
        return companyBrandingService.findDetailsByCompanyId(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @Operation(summary = "Upload company branding logo by tenancy ID")
    @PostMapping(value = "/tenancy/{tenancyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyBranding> uploadLogoByTenancyId(
            @PathVariable String tenancyId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(companyBrandingService.uploadLogoByTenancyId(tenancyId, file));
    }

    @Operation(summary = "Upload company branding logo by company ID")
    @PostMapping(value = "/company/{companyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyBranding> uploadLogoByCompanyId(
            @PathVariable Long companyId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(companyBrandingService.uploadLogoByCompanyId(companyId, file));
    }

    @Operation(summary = "Delete company branding logo by tenancy ID")
    @DeleteMapping("/tenancy/{tenancyId}/logo")
    public ResponseEntity<CompanyBranding> deleteLogoByTenancyId(@PathVariable String tenancyId) {
        return ResponseEntity.ok(companyBrandingService.deleteLogoByTenancyId(tenancyId));
    }

    @Operation(summary = "Delete company branding logo by company ID")
    @DeleteMapping("/company/{companyId}/logo")
    public ResponseEntity<CompanyBranding> deleteLogoByCompanyId(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyBrandingService.deleteLogoByCompanyId(companyId));
    }


    @Operation(summary = "Delete Company Branding by ID",
            description = "Deletes the company branding by its ID.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBrandingById(@PathVariable Long id) {
        boolean deleted = companyBrandingService.deleteBrandingById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }


    @Operation(summary = "Retrieve all Company branding with pagination")
    @GetMapping("/all/{page}/{size}")
    public ResponseEntity<Paginate<CompanyBranding>> getAllBusinessProfiles(
            @PathVariable int page,
            @PathVariable int size) {
        Paginate<CompanyBranding> branding = companyBrandingService.getAllCompanyBranding(page, size);
        return ResponseEntity.ok(branding);
    }

    @Operation(summary = "Delete  Company branding by tenantId")
    @DeleteMapping("/tenant/{tenantId}")
    public ResponseEntity<Void> deleteByTenancyId(@PathVariable String tenantId) {
        companyBrandingService.deleteByTenancyId(tenantId);
        return ResponseEntity.noContent().build();
    }
}
