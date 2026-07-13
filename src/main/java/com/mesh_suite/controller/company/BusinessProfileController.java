package com.mesh_suite.controller.company;

import com.mesh_suite.domain.company.BusinessProfile;
import com.mesh_suite.dto.BusinessProfileDto;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.service.company.BusinessProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mesh-suite/v1.0/business-profiles")
@Tag(name = "Business Profiles")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    @Autowired
    public BusinessProfileController(BusinessProfileService businessProfileService) {
        this.businessProfileService = businessProfileService;
    }

    @Operation(summary = "Create a new business profile")
    @PostMapping(consumes = {"multipart/form-data"})
    public Long createBusinessProfile(@ModelAttribute BusinessProfileDto businessProfileDto) {
        return businessProfileService.createBusinessProfile(businessProfileDto);
    }

    @Operation(summary = "Update an existing business profile")
    @PutMapping
    public BusinessProfile updateBusinessProfile(@RequestBody BusinessProfile profile) {
        return businessProfileService.updateBusinessProfile(profile);
    }

    @Operation(summary = "Retrieve a business profile by ID")
    @GetMapping(value = "/{id}")
    public ResponseEntity<BusinessProfile> getBusinessProfileById(@PathVariable Long id) {
        BusinessProfile businessProfile = businessProfileService.getBusinessProfileById(id);
        return ResponseEntity.ok(businessProfile);
    }
    @Operation(summary = "Retrieve a business profile by USERID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BusinessProfile>> getBusinessProfilesByUserId(@PathVariable Long userId) {
        return Optional.ofNullable(businessProfileService.getBusinessProfilesByUserId(userId))
                .filter(businessProfiles -> !businessProfiles.isEmpty())
                .map(businessProfiles -> ResponseEntity.ok(businessProfiles))  // Return 200 OK if data exists
                .orElseGet(() -> ResponseEntity.noContent().build());  // Return 204 No Content if no data found
    }
    @Operation(summary = "Delete a business profile by ID")
    @DeleteMapping(value = "/{id}")
    public void deleteBusinessProfile(@PathVariable Long id) {
        businessProfileService.deleteBusinessProfile(id);
    }

    @Operation(summary = "Retrieve all business profiles with pagination")
    @GetMapping("/all/{page}/{size}")
    public ResponseEntity<Paginate<BusinessProfile>> getAllBusinessProfiles(
            @PathVariable int page,
            @PathVariable int size) {
        Paginate<BusinessProfile> businessProfilePage = businessProfileService.getAllBusinessProfiles(page, size);
        return ResponseEntity.ok(businessProfilePage);
    }

    @Operation(summary = "Get count of business profiles by gender")
    @GetMapping("/count/gender")
    public ResponseEntity<Map<String, Long>> getCountByGender() {
        Map<String, Long> countByGender = businessProfileService.getCountByGender();
        return new ResponseEntity<>(countByGender, HttpStatus.OK);
    }

        @Operation(summary = "Get count of business profiles by type of business")
    @GetMapping("/count/type-of-business")
        public ResponseEntity<Map<String, Long>> getCountByTypeOfBusiness() {
            Map<String, Long> countByTypeOfBusiness = businessProfileService.getCountByTypeOfBusiness();
            return new ResponseEntity<>(countByTypeOfBusiness, HttpStatus.OK);
        }

    @Operation(summary = "Get count of business profiles by sector")
    @GetMapping("/count/sector")
    public ResponseEntity<Map<String, Long>> getCountBySector() {
        Map<String, Long> countBySector = businessProfileService.getCountBySector();
        return new ResponseEntity<>(countBySector, HttpStatus.OK);
    }
}
