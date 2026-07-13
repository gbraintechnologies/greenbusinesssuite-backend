package com.mesh_suite.service.form;

import com.mesh_suite.dao.form.FormFieldRepository;
import com.mesh_suite.dao.form.FormsRepository;
import com.mesh_suite.domain.form.FormField;
import com.mesh_suite.dto.FormFieldDto;
import com.mesh_suite.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class FormFieldService {
    @Autowired
    private FormFieldRepository formFieldRepository;
    @Autowired
    private FormsRepository formsRepository;

    public FormField updateFormField(FormFieldDto updatedFormField) {
        log.info("Attempting to update FormField with id: {}", updatedFormField.getId());

        Optional<FormField> optionalFormField = formFieldRepository.findById(updatedFormField.getId());

        return optionalFormField.map(formFieldToUpdate -> {
            log.info("FormField found with id: {}. Updating details.", updatedFormField.getId());

            // Update form field with the data from updatedFormField
            formFieldToUpdate.setName(updatedFormField.getName());
            formFieldToUpdate.setDescription(updatedFormField.getDescription());
            formFieldToUpdate.setLabel(updatedFormField.getLabel());
            formFieldToUpdate.setPlaceHolder(updatedFormField.getPlaceHolder());
            formFieldToUpdate.setInstruction(updatedFormField.getInstruction());
            formFieldToUpdate.setOrdering(updatedFormField.getOrdering());
            formFieldToUpdate.setFieldDataType(updatedFormField.getFieldDataType());
            formFieldToUpdate.setChoiceValue(updatedFormField.getChoiceValue());
            formFieldToUpdate.setIsMandatory(updatedFormField.getIsMandatory());
            formFieldToUpdate.setHorizontalAlign(updatedFormField.getHorizontalAlign());
            formFieldToUpdate.setValidPattern(updatedFormField.getValidPattern());
            formFieldToUpdate.setUpdatedOn(LocalDateTime.now());
            formFieldToUpdate.setIsDeleted(updatedFormField.getIsDeleted());
            formFieldToUpdate.setIsStatisticalField(updatedFormField.getIsStatisticalField());
            formFieldToUpdate.setStatisticalFunction(updatedFormField.getStatisticalFunction());
            formFieldToUpdate.setDisplayType(updatedFormField.getDisplayType());
            formFieldToUpdate.setMaxLength(updatedFormField.getMaxLength());

            // Save and return the updated form field
            FormField savedFormField = formFieldRepository.save(formFieldToUpdate);
            log.info("Successfully updated FormField with id: {}", updatedFormField.getId());
            return savedFormField;
        }).orElseThrow(() -> {
            log.warn("FormField not found with id: {}", updatedFormField.getId());
            return new ResourceNotFoundException("Form field not found with id: " + updatedFormField.getId());
        });
    }
    public void deleteFormFieldById(Long id) {
        log.info("Attempting to delete FormField with id: {}", id);
        Optional<FormField> optionalFormField = formFieldRepository.findById(id);

        if (optionalFormField.isPresent()) {
            formFieldRepository.delete(optionalFormField.get());
            log.info("Successfully deleted FormField with id: {}", id);
        } else {
            log.warn("FormField not found with id: {}", id);
            throw new ResourceNotFoundException("Form field not found with id: " + id);
        }
    }
}