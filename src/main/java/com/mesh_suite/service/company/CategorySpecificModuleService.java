package com.mesh_suite.service.company;

import com.mesh_suite.dao.company.CategorySpecificModuleRepository;
import com.mesh_suite.domain.company.CategorySetup;
import com.mesh_suite.domain.company.CategorySpecificModule;
import com.mesh_suite.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class CategorySpecificModuleService {

    private final CategorySpecificModuleRepository categorySpecificModuleRepository;
    private final CategorySetupService categorySetupService;

    public CategorySpecificModuleService(CategorySpecificModuleRepository categorySpecificModuleRepository,
                                         CategorySetupService categorySetupService) {
        this.categorySpecificModuleRepository = categorySpecificModuleRepository;
        this.categorySetupService = categorySetupService;
    }

    // Save a new CategorySpecificModule
    @Transactional
    public CategorySpecificModule saveCategorySpecificModule(CategorySpecificModule categorySpecificModule) {
        return categorySpecificModuleRepository.save(categorySpecificModule);
    }

    // Get all modules for a specific category
    public Set<CategorySpecificModule> getCategorySpecificModules(Long categoryId) {
        CategorySetup categorySetup = categorySetupService.getCategoryById(categoryId);
        return categorySetup.getCategorySpecificModules();
    }
    @Transactional
    public CategorySpecificModule updateCategorySpecificModule(CategorySpecificModule updatedModule) {
        CategorySpecificModule existingModule = categorySpecificModuleRepository.findById(updatedModule.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CategorySpecificModule not found with id: " + updatedModule.getId()));

        existingModule.setModuleName(updatedModule.getModuleName());
        existingModule.setAdminFeatures(updatedModule.getAdminFeatures());
        existingModule.setClientFeatures(updatedModule.getClientFeatures());
        existingModule.setTemplate(updatedModule.isTemplate());
        //existingModule.setCategory(updatedModule.getCategory()); // Ensure proper handling for category associations

        return categorySpecificModuleRepository.save(existingModule);
    }

    // Delete a specific CategorySpecificModule from a category
    @Transactional
    public void deleteCategorySpecificModule(Long categoryId, Long moduleId) {
        CategorySpecificModule module = categorySpecificModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        
        if (!module.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException("Module does not belong to the specified category.");
        }

        categorySpecificModuleRepository.delete(module);
    }

}
