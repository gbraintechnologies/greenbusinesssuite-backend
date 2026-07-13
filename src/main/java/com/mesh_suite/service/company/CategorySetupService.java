package com.mesh_suite.service.company;

import com.mesh_suite.dao.company.CategorySetupRepository;
import com.mesh_suite.dao.company.CategorySpecificModuleRepository;
import com.mesh_suite.dao.company.ModuleRepository;
import com.mesh_suite.domain.company.CategorySetup;
import com.mesh_suite.domain.company.CategorySpecificModule;
import com.mesh_suite.domain.company.Module;
import com.mesh_suite.dto.CategorySetupDTO;
import com.mesh_suite.dto.CategorySpecificModuleDTO;
import com.mesh_suite.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategorySetupService {
    private final CategorySetupRepository categorySetupRepository;
    private final ModuleRepository moduleRepository;
    private final CategorySpecificModuleRepository categorySpecificModuleRepository;
    public CategorySetupService(CategorySetupRepository categorySetupRepository,
                                ModuleRepository moduleRepository,
                                CategorySpecificModuleRepository categorySpecificModuleRepository) {
        this.categorySetupRepository = categorySetupRepository;
        this.moduleRepository = moduleRepository;
        this.categorySpecificModuleRepository = categorySpecificModuleRepository;
    }

    @Transactional
    public Long createCategory(CategorySetup categorySetup) {
        return categorySetupRepository.save(categorySetup).getId();
    }



    public CategorySetup getCategoryById(Long id) {
        return categorySetupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public List<CategorySetup> getAllCategories() {
        return categorySetupRepository.findAll();
    }
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }
    @Transactional
    public CategorySetup updateCategory(CategorySetup categorySetup) {

        CategorySetup existingCategory = getCategoryById(categorySetup.getId());

        existingCategory.setCategoryName(categorySetup.getCategoryName());
        existingCategory.setCategoryDescription(categorySetup.getCategoryDescription());

        // Update the category-specific modules if present
        if (categorySetup.getCategorySpecificModules() != null) {
            categorySetup.getCategorySpecificModules().forEach(newModule -> {
                // Find the existing module with the same id
                Optional<CategorySpecificModule> existingModuleOpt = existingCategory.getCategorySpecificModules().stream()
                        .filter(module -> module.getId().equals(newModule.getId()))
                        .findFirst();

                // If the module exists, update it; otherwise, add the new module
                existingModuleOpt.ifPresentOrElse(existingModule -> {
                    // Update existing module
                    existingModule.setModuleName(newModule.getModuleName());
                    existingModule.setAdminFeatures(newModule.getAdminFeatures());
                    existingModule.setClientFeatures(newModule.getClientFeatures());
                    existingModule.setTemplate(newModule.isTemplate());
                }, () -> {
                    // Add new module if not present
                    newModule.setCategory(existingCategory); // Set the category reference
                    existingCategory.getCategorySpecificModules().add(newModule); // Add to the set
                });
            });
        }

        return categorySetupRepository.save(existingCategory);
    }


    public void deleteCategory(Long id) {categorySetupRepository.delete(getCategoryById(id));
    }
    public void deleteModule(Long id) {moduleRepository.delete(getModuleById(id));
    }
    public Long createModule(Module module) {
        return moduleRepository.save(module).getId();
    }
    public Module getModuleById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Core Module not found with id : "+id));
    }
    public Module updateModule(Module module) {
        Module existingModule = getModuleById(module.getId());
        existingModule.setModuleName(module.getModuleName());
        existingModule.setModuleDescription(module.getModuleDescription());
       existingModule.setAdminFeatures(module.getAdminFeatures());
       existingModule.setClientFeatures(module.getClientFeatures());
        return moduleRepository.save(existingModule);
    }

    public List<CategorySetup> searchCategoriesByName(String categoryName) {
        return categorySetupRepository.findByCategoryNameContainingIgnoreCase(categoryName);
    }
    public List<Module> searchModuleByName(String moduleName) {
        return moduleRepository.findByModuleNameContainingIgnoreCase(moduleName);
    }

    @Transactional
    public CategorySetupDTO findCategoryByModuleId(Long moduleId) {

        CategorySpecificModule categorySpecificModule = categorySpecificModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("CategorySpecificModule not found with id: " + moduleId));

        CategorySetup categorySetup = categorySpecificModule.getCategory();

        Set<CategorySpecificModuleDTO> moduleDTOs = categorySetup.getCategorySpecificModules().stream()
                .map(module -> new CategorySpecificModuleDTO(module.getId(), module.getModuleName(),
                        module.getAdminFeatures(), module.getClientFeatures(), module.isTemplate()))
                .collect(Collectors.toSet());

        return new CategorySetupDTO(categorySetup.getId(), categorySetup.getCategoryName(),
                categorySetup.getCategoryDescription(), categorySetup.getCreatedOn(),
                categorySetup.getUpdatedOn(), moduleDTOs);
    }

}
