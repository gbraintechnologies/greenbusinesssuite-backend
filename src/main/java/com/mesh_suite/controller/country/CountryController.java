package com.mesh_suite.controller.country;

import com.mesh_suite.domain.coutry.Country;
import com.mesh_suite.dto.CountryRequestDTO;
import com.mesh_suite.dto.PaginatedCountryResponse;
import com.mesh_suite.dto.ParentLevelRequestDTO;
import com.mesh_suite.exception.CountryNotFoundException;
import com.mesh_suite.service.country.CountryService;
import com.mesh_suite.service.notify.CsvService;
import com.opencsv.exceptions.CsvException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("mesh-suite/v1.0/countries")
@Tag(name = "Country Setup Management", description = "APIs for managing countries")
public class CountryController {

    @Autowired
    private CountryService countryService;
    @Autowired
    private CsvService csvService;
    @PostMapping
    @Operation(summary = "Create a new country setup")
    public ResponseEntity<Long> createCountry(@RequestBody CountryRequestDTO request) {
        return ResponseEntity.ok(countryService.createCountry(request));
    }


    @GetMapping("/by-id/{id}")
    @Operation(summary = "Retrieve a country by ID with associated entities")
    public ResponseEntity<Country> getCountryById(@PathVariable Long id) {
        Country country = countryService.getCountryById(id);
        return ResponseEntity.ok(country);
    }

    @PutMapping
    @Operation(summary = "Update a country with associated entities")
    public ResponseEntity<Country> updateCountry(@RequestBody Country updatedCountry) {
       return ResponseEntity.ok(countryService.updateCountry(updatedCountry));
    }
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete a country by ID with associated entities")
    public ResponseEntity<?> deleteCountryById(@PathVariable Long id) {
        try {
            countryService.deleteCountryById(id);
            return ResponseEntity.noContent().build();
        } catch (CountryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Retrieve paginated list of countries")
    @GetMapping("/all/{page}/{size}")
    public PaginatedCountryResponse getCountries(
            @PathVariable int page,
            @PathVariable int size
    ) {
        return countryService.getCountries(page, size);
    }
    @Operation(summary = "Retrieve a country by name (case-insensitive)")
    @GetMapping("/name/{countryName}")
    public ResponseEntity<Country> getCountryByName(@PathVariable String countryName) {
        return countryService.getCountryByName(countryName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Retrieve all child levels entries by parent name and country ID")
    @GetMapping("/child-entries/{parentName}/{countryId}")
    public ResponseEntity<List<String>> getChildLevelsByParentNameAndCountryId(
            @PathVariable String parentName,
            @PathVariable Long countryId) {
        List<String> childLevels = countryService.getChildLevelsByParentNameAndCountryId(parentName, countryId);
        if (childLevels.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(childLevels);
    }
    @Operation(summary = "Import country data from CSV")
    @PostMapping(value = "/csv-import",consumes = {"multipart/form-data"})
    public ResponseEntity<String> importCsv(@RequestPart("file") MultipartFile file) {
        try {
            csvService.importCountryCsv(file);
            return ResponseEntity.ok("CSV file successfully processed");
        } catch (IOException | CsvException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing CSV file: " + e.getMessage());
        }
    }
    @Operation(summary = "Add a new parent level with child levels to an existing country")
    @PutMapping("/add-parent-level")
    public ResponseEntity<String> addParentLevel(@RequestBody ParentLevelRequestDTO parentLevelRequest) {
        countryService.addParentLevel(parentLevelRequest);
        return ResponseEntity.ok("Parent level and child levels added successfully");
    }

    @Operation(summary = "Delete a parent level from an existing country")
    @DeleteMapping("/delete-parent-level/{parentId}")
    public ResponseEntity<String> deleteParentLevel(@PathVariable Long parentId) {
        countryService.deleteParentLevel(parentId);
        return ResponseEntity.ok("Parent level deleted successfully");
    }
    @Operation(
            summary = "Retrieve all country names",
            description = "This endpoint returns a list of all country names stored in the database."
    )
    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllCountryNames() {
        List<String> countryNames = countryService.getAllCountryNames();
        return ResponseEntity.ok(countryNames);
    }
    @Operation(
            summary = "Retrieve all child levels by parent name",
            description = "This endpoint returns a list of all child levels associated with a given parent name. The search is case-insensitive."
    )
    @GetMapping("/child-levels/{parentName}")
    public ResponseEntity<List<String>> getChildLevelsByParentName(@PathVariable String parentName) {
        List<String> childLevels = countryService.getChildLevelsByParentName(parentName);
        return ResponseEntity.ok(childLevels);
    }
}
