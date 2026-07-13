package com.mesh_suite.controller.company;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.dto.CompanyRegResp;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.dto.request.*;
import com.mesh_suite.dto.response.CompanyResponseDTO;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.service.company.CompanyDetailService;
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
        return ResponseEntity.ok(companyDetailService.getById(id));
    }

    @Operation(summary = "Search User Company by Company Name")
    @GetMapping("/get-company-by-name/{name}")
    public ResponseEntity<CompanyResponseDTO> searchCompanyByName(
            @PathVariable @NotBlank String name) {
        return ResponseEntity.ok(companyDetailService.searchByName(name));
    }

    @Operation(summary = "Search User Company by Status")
    @GetMapping("/filter/status")
    public ResponseEntity<Paginate<CompanyResponseDTO>> filterCompanyByStatus(
            @RequestParam CompanyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(companyDetailService.filterByStatus(status, pageable));
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
        return ResponseEntity.ok(companyDetailService.getAllCompanies(pageable));
    }

    @Operation(summary = "Get companies by user's company identifier")
    @GetMapping("/user/{companyIdentifier}")
    public ResponseEntity<Paginate<CompanyResponseDTO>> getCompaniesByUserIdentifier(
            @PathVariable String companyIdentifier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(companyDetailService.getCompaniesByUserIdentifier(companyIdentifier, pageable));
    }

    @Operation(summary = "Get companies by user ID")
    @GetMapping("/user/company/{userId}")
    public ResponseEntity<Paginate<CompanyResponseDTO>> getCompaniesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(companyDetailService.getCompaniesByUserId(userId, pageable));
    }

}
