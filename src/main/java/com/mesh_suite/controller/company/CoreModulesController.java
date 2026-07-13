package com.mesh_suite.controller.company;

import com.mesh_suite.domain.company.Module;
import com.mesh_suite.service.company.CategorySetupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/mesh-suite/v1.0/core-modules")
@Tag(name = "Core Modules Setup", description = "Endpoints for managing Core Modules")
public class CoreModulesController {

    private final CategorySetupService categorySetupService;

    public CoreModulesController(CategorySetupService categorySetupService) {
        this.categorySetupService = categorySetupService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new core module")
    public ResponseEntity<Long> createModule(@RequestBody Module module) {
        return new ResponseEntity<>(categorySetupService.createModule(module), HttpStatus.CREATED);
    }

    @GetMapping("/get-modules")
    @Operation(summary = "Get all core modules")
    public ResponseEntity<List<Module>> getAllModules() {
        List<Module> categories = categorySetupService.getAllModules();
        return ResponseEntity.ok(categories);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete core module by ID")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        categorySetupService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-module/{id}")
    @Operation(summary = "Get core module by ID")
    public ResponseEntity<Module> getModuleById(@Parameter(description = "ID of the module to fetch") @PathVariable Long id) {
        return ResponseEntity.ok(categorySetupService.getModuleById(id)) ;
    }

    @PutMapping("/update")
    @Operation(summary = "Update core module")
    public ResponseEntity<Module> updateModule(@RequestBody Module updatedModule) {
            return ResponseEntity.ok(categorySetupService.updateModule(updatedModule));
        }
    @Operation(summary = "Search core module by name" )
    @GetMapping("/module-name-search/{moduleName}")
    public ResponseEntity<List<Module>> searchModuleByName(
            @Parameter(description = "Module name to search (case-insensitive)", required = true)
            @PathVariable String moduleName) {

        List<Module> modules = categorySetupService.searchModuleByName(moduleName);
        return ResponseEntity.ok(modules);
    }
}

