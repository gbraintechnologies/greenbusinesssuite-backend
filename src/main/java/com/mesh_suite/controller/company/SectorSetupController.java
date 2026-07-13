package com.mesh_suite.controller.company;

import com.mesh_suite.domain.company.SectorSetup;
import com.mesh_suite.domain.company.Sectors;
import com.mesh_suite.dto.PaginatedSectorResponse;
import com.mesh_suite.dto.SectorSetupDTO;
import com.mesh_suite.dto.SectorSetupRequestDto;
import com.mesh_suite.dto.SectorsResponse;
import com.mesh_suite.service.company.SectorSetupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/mesh-suite/v1.0/sectors")
@Tag(name = "Sector Setup Management", description = "Endpoints for managing sector setup data")
@Slf4j
public class SectorSetupController {
    @Autowired
    private SectorSetupService sectorSetupService;

    @GetMapping("/sectors/{page_number}/{page_size}")
    @Operation(summary = "Get all sector setups")
    public ResponseEntity<PaginatedSectorResponse> getAllSectorSetups(
            @Parameter(description = "Page number") @PathVariable(name = "page_number") Integer pageNumber,
            @Parameter(description = "Page size") @PathVariable(name = "page_size") Integer pageSize) {

        Page<SectorSetup> sectorSetupsPage = sectorSetupService.getAllSectorSetups(pageNumber - 1, pageSize); // Adjusting page number to zero-based index
        PaginatedSectorResponse response = new PaginatedSectorResponse();
        response.setFirst(sectorSetupsPage.isFirst());
        response.setLast(sectorSetupsPage.isLast());
        response.setTotalElements(sectorSetupsPage.getTotalElements());
        response.setTotalPages(sectorSetupsPage.getTotalPages());
        response.setSize(sectorSetupsPage.getSize());
        response.setContent(sectorSetupsPage.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/data/{id}")
    @Operation(summary = "Get Sector Setup data by ID", description = "Retrieve sector setup information based on the specified ID.")
    public ResponseEntity<SectorSetup> getSectorSetupById(@PathVariable(name = "id") Long id) {
        SectorSetup sectorSetup = sectorSetupService.getSectorSetupById(id);
        return ResponseEntity.ok(sectorSetup);
    }
    @GetMapping("/info/{countryName}")
    @Operation(summary = "Get Sector Statistics data by Country", description = "Retrieve sector statistics based on the specified country name.")
    public ResponseEntity<List<Map<String, Object>>> getSectorStatisticsByCountryName(@PathVariable(name = "countryName") String countryName) {
        List<Map<String, Object>> statistics = sectorSetupService.getSectorStatisticsByCountryName(countryName);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get SectorSetup by Country Name", description = "Retrieve a list of SectorSetup entities by country name, ensuring case insensitivity")
    @GetMapping("/data-by-country/{countryName}")
    public ResponseEntity<List<SectorSetup>> getSectorSetupByCountryName(@PathVariable(name = "countryName") String countryName) {
        List<SectorSetup> sectorSetups = sectorSetupService.getSectorSetupByCountryName(countryName);
        return ResponseEntity.ok(sectorSetups);
    }
    @PutMapping("/update")
    @Operation(summary = "Update a sector setup", description = "Update an existing sector setup with the provided details.")
    public ResponseEntity<SectorSetup> updateSectorSetup(@RequestBody SectorSetup sectorSetup) {
        sectorSetup.setId(sectorSetup.getId());
        Optional<SectorSetup> updatedSectorSetup = sectorSetupService.updateSectorSetup(sectorSetup);
        return updatedSectorSetup.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sector setup by id")
    public ResponseEntity<Void> deleteSectorSetup(@PathVariable Long id) {
        sectorSetupService.deleteSectorSetup(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/edit/{subSectorId}")
    @Operation(summary = "Edit sub sector by id")
    public ResponseEntity<Sectors> editSubSector(@PathVariable Long subSectorId, @RequestBody Set<String> newSubSectors) {
        log.info("Editing sector for sector with ID: {}", subSectorId);
        Sectors updatedSector = sectorSetupService.editSubSector(subSectorId, newSubSectors);
        return ResponseEntity.ok(updatedSector);
    }

    @DeleteMapping("/delete/{subSectorId}")
    @Operation(summary = "Delete sub sector by id")
    public ResponseEntity<Void> deleteSubSector(@PathVariable Long subSectorId) {
        log.info("Deleting sub sectors for sector with ID: {}", subSectorId);
        sectorSetupService.deleteSectorById(subSectorId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{subSectorId}")
    @Operation(summary = "Retrieve sub sector by id")
    public ResponseEntity<Sectors> getSector(@PathVariable Long subSectorId) {
        log.info("Fetching sector with ID: {}", subSectorId);
        Sectors sector = sectorSetupService.getSector(subSectorId);
        return ResponseEntity.ok(sector);   
    }

    @GetMapping("/details/{sectorSetupId}/{sectorId}")
    @Operation(summary = "Get details of a specific sector in a sector setup")
    public ResponseEntity<Map<String, Object>> getSectorDetails(@PathVariable Long sectorSetupId, @PathVariable Long sectorId) {
        Map<String, Object> sectorDetails = sectorSetupService.getSectorDetailsByIds(sectorSetupId, sectorId);
        return ResponseEntity.ok(sectorDetails);
    }

    @Operation(summary = "Create a Sector Setup", description = "Creates a new Sector Setup or updates an existing one based on the provided details.")
    @PostMapping("/create")
    public ResponseEntity<Long> createSectorSetup(@RequestBody SectorSetupRequestDto requestDto) {
        Long sectorSetupId = sectorSetupService.createSectorSetup(requestDto);
        return new ResponseEntity<>(sectorSetupId, HttpStatus.CREATED);
    }

    @Operation(summary = "Deletes all sectors associated with the specified SectorSetup ID.")
    @DeleteMapping("/delete-all/{sectorSetupId}")
    public ResponseEntity<Void> deleteSectorsBySectorSetupId(@PathVariable Long sectorSetupId) {
        sectorSetupService.deleteSectorsBySectorSetupId(sectorSetupId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/all/info-data/{page_number}/{page_size}")
    @Operation(summary = "Get all sector setups with pagination", description = "Retrieve all sector setups with parent sectors and sub-sector counts, paginated.")
    @ApiResponse(responseCode = "200", description = "Paginated sector setups")
    public ResponseEntity<SectorsResponse> getAllSectorData(
            @Parameter(description = "Page number (zero-based index)") @PathVariable(name = "page_number") Integer pageNumber,
            @Parameter(description = "Page size") @PathVariable(name = "page_size") Integer pageSize) {

        Page<SectorSetupDTO> sectorSetupsPage = sectorSetupService.getAllSectorData(pageNumber - 1, pageSize); // Adjusting page number to zero-based index

        SectorsResponse response = new SectorsResponse();
        response.setFirst(sectorSetupsPage.isFirst());
        response.setLast(sectorSetupsPage.isLast());
        response.setTotalElements(sectorSetupsPage.getTotalElements());
        response.setTotalPages(sectorSetupsPage.getTotalPages());
        response.setSize(sectorSetupsPage.getSize());
        response.setContent(sectorSetupsPage.getContent());

        return ResponseEntity.ok(response);
    }

}