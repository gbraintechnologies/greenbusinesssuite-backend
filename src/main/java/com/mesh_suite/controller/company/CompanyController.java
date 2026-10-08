package com.mesh_suite.controller.company;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.dto.CompanyRegResp;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.dto.request.*;
import com.mesh_suite.dto.response.CompanyResponseDTO;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.dto.request.LegacyCompanyWriteRequest;
import com.mesh_suite.service.company.CompanyCompatibilityService;
import com.mesh_suite.service.company.CompanyDetailService;
import com.mesh_suite.service.company.CompanyResponseEnricher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/mesh-suite/v1.0/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Management", description = "Operations related to managing companies in the system")
public class CompanyController {

    private final CompanyDetailService companyDetailService;
    private final CompanyCompatibilityService companyCompatibilityService;
    private final CompanyResponseEnricher companyResponseEnricher;

    @Operation(summary = "Register new Company Account")
    @PostMapping("/create")
    public ResponseEntity<CompanyRegResp> createCompany(
            @RequestBody @Valid CompanyCreateDTO request) throws UnsupportedEncodingException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyDetailService.createCompany(request));
    }

    @Operation(summary = "Get company by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> getCompanyDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(companyResponseEnricher.enrich(companyDetailService.getById(id)));
    }

    @Operation(summary = "Search User Company by Company Name")
    @GetMapping("/get-company-by-name/{name}")
    public ResponseEntity<CompanyResponseDTO> searchCompanyByName(
            @PathVariable @NotBlank String name) {
        return ResponseEntity.ok(companyResponseEnricher.enrich(companyDetailService.searchByName(name)));
    }

    @Operation(summary = "Search User Company by Status")
    @GetMapping("/filter/status")
    public ResponseEntity<Paginate<CompanyResponseDTO>> filterCompanyByStatus(
            @RequestParam CompanyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Paginate<CompanyResponseDTO> result = companyDetailService.filterByStatus(status, pageable);
        companyResponseEnricher.enrich(result.getContent());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Update User Company")
    @PutMapping
    public ResponseEntity<CompanyUpdateDTO> updateCompanyDetail(
            @RequestBody @Valid CompanyUpdateDTO companyDetailDTO) {
        return ResponseEntity.ok(companyDetailService.update(companyDetailDTO));
    }

    @Operation(summary = "Update company status")
    @PutMapping("/status")
    public ResponseEntity<CompanyUpdateDTO> updateCompanyStatus(
            @RequestBody @Valid UpdateCompanyStatusDTO request) {
        return ResponseEntity.ok(companyDetailService.updateCompanyStatus(request));
    }

    @Operation(summary = "Update company's assigned form set")
    @PutMapping("/forms")
    public ResponseEntity<CompanyUpdateDTO> updateCompanyForms(
            @RequestBody @Valid UpdateCompanyFormsDTO request) {
        return ResponseEntity.ok(companyDetailService.updateCompanyFormSet(request));
    }

    @Operation(summary = "Assign company admin to tenant")
    @PutMapping("/admin")
    public ResponseEntity<MessageResponse> assignTenantAdmin( @Valid @RequestBody CreateUserRequest request) {
        MessageResponse response = companyDetailService.assignTenantAdmin(request);
        return ResponseEntity.ok(response);
    }

    // In CompanyController.java
    @Operation(summary = "Get all companies")
    @GetMapping
    public ResponseEntity<Paginate<CompanyResponseDTO>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Paginate<CompanyResponseDTO> result = companyDetailService.getAllCompanies(pageable);
        companyResponseEnricher.enrich(result.getContent());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get companies by user's company identifier")
    @GetMapping("/user/{companyIdentifier}")
    public ResponseEntity<Paginate<CompanyResponseDTO>> getCompaniesByUserIdentifier(
            @PathVariable String companyIdentifier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Paginate<CompanyResponseDTO> result = companyDetailService.getCompaniesByUserIdentifier(companyIdentifier, pageable);
        companyResponseEnricher.enrich(result.getContent());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get companies by user ID")
    @GetMapping("/user/company/{userId}")
    public ResponseEntity<Paginate<CompanyResponseDTO>> getCompaniesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Paginate<CompanyResponseDTO> result = companyDetailService.getCompaniesByUserId(userId, pageable);
        companyResponseEnricher.enrich(result.getContent());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Update a company together with its custom profile values and SMS sender id")
    @PutMapping("/edit_with_custom_fields/{companyId}")
    public ResponseEntity<CompanyResponseDTO> editWithCustomFields(
            @PathVariable Long companyId,
            @RequestBody LegacyCompanyWriteRequest request) {
        CompanyResponseDTO updated = companyCompatibilityService.editWithCustomFields(companyId, request);
        return ResponseEntity.ok(companyResponseEnricher.enrich(updated));
    }

}
