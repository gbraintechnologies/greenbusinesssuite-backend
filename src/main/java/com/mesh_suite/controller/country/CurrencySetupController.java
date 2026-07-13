package com.mesh_suite.controller.country;

import com.mesh_suite.domain.form.CurrencySetup;
import com.mesh_suite.dto.PaginatedCurrency;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.service.form.CurrencySetupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mesh-suite/v1.0/forms/currency-setup")
@Tag(name = "Currency and Denominations Config", description = "Currency and Denomination Setups ")
public class CurrencySetupController {

    @Autowired
    private CurrencySetupService currencySetupService;

    @PostMapping
    @Operation(summary = "Create a new currency setup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Currency setup created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> createCurrencySetup(@RequestBody CurrencySetup currencySetup) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(currencySetupService.createCurrencySetup(currencySetup).getId());
    }

    @GetMapping("/all/{page_number}/{page_size}")
    @Operation(summary = "Retrieve all currency setups (including deleted)")
    public ResponseEntity<PaginatedCurrency> getCurrencySetups(
            @Parameter(description = "Page number") @PathVariable(name = "page_number") Integer pageNumber,
            @Parameter(description = "Page size") @PathVariable(name = "page_size") Integer pageSize) {

        Page<CurrencySetup> currencySetupsPage = currencySetupService.getCurrencySetups(pageNumber - 1, pageSize);
        PaginatedCurrency response = new PaginatedCurrency();
        response.setFirst(currencySetupsPage.isFirst());
        response.setLast(currencySetupsPage.isLast());
        response.setTotalElements(currencySetupsPage.getTotalElements());
        response.setTotalPages(currencySetupsPage.getTotalPages());
        response.setSize(currencySetupsPage.getSize());
        response.setContent(currencySetupsPage.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/existing/{page_number}/{page_size}")
    @Operation(summary = "Get all existing currency setups (non-deleted) with pagination")
    public ResponseEntity<PaginatedCurrency> getAllExistingCurrencySetups(
            @Parameter(description = "Page number") @PathVariable(name = "page_number") Integer pageNumber,
            @Parameter(description = "Page size") @PathVariable(name = "page_size") Integer pageSize) {

        Page<CurrencySetup> currencySetupsPage = currencySetupService.getAllExistingCurrencySetups(pageNumber - 1, pageSize);
        PaginatedCurrency response = new PaginatedCurrency();
        response.setFirst(currencySetupsPage.isFirst());
        response.setLast(currencySetupsPage.isLast());
        response.setTotalElements(currencySetupsPage.getTotalElements());
        response.setTotalPages(currencySetupsPage.getTotalPages());
        response.setSize(currencySetupsPage.getSize());
        response.setContent(currencySetupsPage.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary ="Get currency setup by ID")
    public ResponseEntity<?> getCurrencySetupById(@PathVariable(name = "id") Long id) {
        CurrencySetup currencySetup = currencySetupService.getCurrencySetupById(id);
        return ResponseEntity.ok(currencySetup);
    }

    @PutMapping("/update")
    @Operation(summary ="Update an existing currency setup")
    public ResponseEntity<?> updateCurrencySetup( @RequestBody CurrencySetup currencySetup) {
        if(currencySetup.getId()==null){
            throw new ResourceNotFoundException("Currency Not Found with id :"+ currencySetup.getId());
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(( currencySetupService.updateCurrencySetup(currencySetup)));

    }

    @DeleteMapping("/hard-delete/{id}")
    @Operation(summary = "Delete currency setup by ID")
    public ResponseEntity<?> deleteCurrencySetup(@PathVariable(name = "id") Long id) {
        if(id ==null){
            throw new ResourceNotFoundException("Currency Not Found with id :"+ id);
        }
        currencySetupService.deleteCurrencySetup(id);
        return ResponseEntity.status(HttpStatus.OK).body("Currency Setup permanently deleted with id "+id);
    }

    @DeleteMapping("/soft-delete/{id}")
    @Operation(summary = "Delete currency setup by ID")
    public ResponseEntity<?> softDeleteCurrencySetup(@PathVariable(name = "id") Long id) {
        if(id ==null){
            throw new ResourceNotFoundException("Currency Not Found with id :"+ id);
        }
        currencySetupService.softDeleteCurrencySetup(id);
        return ResponseEntity.status(HttpStatus.OK).body("Currency Setup deleted with id "+id);
    }

    @GetMapping("/by-country/{countryName}")
    @Operation(summary = "Find currency setups by country name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found currency setups"),
            @ApiResponse(responseCode = "404", description = "No currency setups found for the given country name")
    })
    public ResponseEntity<List<CurrencySetup>> findCurrencyByCountryName(
            @Parameter(description = "Name of the country to search for currency setups")
            @PathVariable String countryName) {
        List<CurrencySetup> currencySetups = currencySetupService.findCurrencyByCountryName(countryName);
        return new ResponseEntity<>(currencySetups, HttpStatus.OK);
    }
    @GetMapping("/search/{currency}")
    @Operation(summary = "Search currency setups by currency name", description = "Retrieve currency setups by currency name, case insensitive.")
    public ResponseEntity<List<CurrencySetup>> searchByCurrency(
            @Parameter(description = "The name of the currency to search for") @PathVariable(name = "currency") String currency) {
        List<CurrencySetup> currencySetups = currencySetupService.searchByCurrency(currency);
        return ResponseEntity.ok(currencySetups);
    }
}
