package com.mesh_suite.controller.company;

import com.mesh_suite.domain.company.CategorySetup;
import com.mesh_suite.domain.company.CategorySpecificModule;
import com.mesh_suite.dto.CategorySetupDTO;
import com.mesh_suite.service.company.CategorySetupService;
import com.mesh_suite.service.company.CategorySpecificModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/mesh-suite/v1.0/specific-category-module")
@Tag(name = "Category Specific Modules Setup", description = "Endpoints for managing Category Specific Module")
public class CategorySetupController {

    private final CategorySetupService categorySetupService;
    private final CategorySpecificModuleService categorySpecificModuleService;

    public CategorySetupController(CategorySetupService categorySetupService,
                                   CategorySpecificModuleService categorySpecificModuleService) {
        this.categorySetupService = categorySetupService;
        this.categorySpecificModuleService = categorySpecificModuleService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new category")
    public ResponseEntity<Long> createCategory(@RequestBody CategorySetup categorySetup) {
        return new ResponseEntity<>(categorySetupService.createCategory(categorySetup), HttpStatus.CREATED);
    }
    @GetMapping("/get-category/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategorySetup> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categorySetupService.getCategoryById(id));
    }
    @PostMapping("/add-module/{categoryId}")
    @Operation(summary = "Add specific module to the category by ID", description = "Adds a new module to the specified category.")
    public ResponseEntity<Long> addModuleToCategory(
            @Parameter(description = "ID of the category to which module will be added")
            @PathVariable Long categoryId,
            @RequestBody CategorySpecificModule categorySpecificModule) {
        
        CategorySetup categorySetup = categorySetupService.getCategoryById(categoryId);
        categorySpecificModule.setCategory(categorySetup);
        CategorySpecificModule savedModule = categorySpecificModuleService.saveCategorySpecificModule(categorySpecificModule);

        return new ResponseEntity<>(savedModule.getId(), HttpStatus.CREATED);
    }
    @GetMapping("/all")
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategorySetup>> getAllCategories() {
        List<CategorySetup> categories = categorySetupService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    @GetMapping("/get-all-modules/{categoryId}")
    @Operation(summary = "Get all category specific modules for a category", description = "Fetches all modules associated with the specified category.")
    public ResponseEntity<Set<CategorySpecificModule>> getCategorySpecificModules(
            @Parameter(description = "ID of the category to fetch modules for")
            @PathVariable Long categoryId) {
        
        Set<CategorySpecificModule> modules = categorySpecificModuleService.getCategorySpecificModules(categoryId);
        return new ResponseEntity<>(modules, HttpStatus.OK);
    }
    @PutMapping("/update")
    @Operation(summary = "Update category")
    public ResponseEntity<CategorySetup> updateCategory(@RequestBody CategorySetup categorySetup) {
        return ResponseEntity.ok(categorySetupService.updateCategory(categorySetup));
    }
    @DeleteMapping("/category-delete/{id}")
    @Operation(summary = "Delete category by ID")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categorySetupService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/modify")
    @Operation(summary = "Update a category specific module")
    public ResponseEntity<CategorySpecificModule> updateCategorySpecificModule(
            @RequestBody CategorySpecificModule categorySpecificModule) {
        CategorySpecificModule updatedModule = categorySpecificModuleService.updateCategorySpecificModule(categorySpecificModule);
        return ResponseEntity.ok(updatedModule);
    }
    @DeleteMapping("/delete-specific-module/{categoryId}/{moduleId}")
    @Operation(summary = "Delete a specific module from a category", description = "Deletes a module from the specified category.")
    public ResponseEntity<Void> deleteCategorySpecificModule(
            @Parameter(description = "ID of the category from which module will be deleted")
            @PathVariable Long categoryId,
            @Parameter(description = "ID of the module to be deleted")
            @PathVariable Long moduleId) {
        
        categorySpecificModuleService.deleteCategorySpecificModule(categoryId, moduleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Search categories by name" )
    @GetMapping("/category-name-search/{categoryName}")
    public ResponseEntity<List<CategorySetup>> searchCategoriesByName(
            @Parameter(description = "Category name to search (case-insensitive)", required = true)
            @PathVariable String categoryName) {

        List<CategorySetup> categories = categorySetupService.searchCategoriesByName(categoryName);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/retrieve/by-module-id/{moduleId}")
    @Operation(summary = "Retrieve CategorySetup from which the Category Specific Module belongs to by ID")
    public ResponseEntity<CategorySetupDTO> getCategoryByModuleId(@PathVariable Long moduleId) {
        CategorySetupDTO categorySetupDTO = categorySetupService.findCategoryByModuleId(moduleId);
        return ResponseEntity.ok(categorySetupDTO);
    }


}
