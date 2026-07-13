package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.dao.form.FormDataRepository;
import com.mesh_suite.dao.form.FormFieldRepository;
import com.mesh_suite.dao.form.FormSectionRepository;
import com.mesh_suite.dao.form.FormsRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.domain.form.FormField;
import com.mesh_suite.domain.form.FormSections;
import com.mesh_suite.domain.form.Forms;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.FormElementOrderingDto;
import com.mesh_suite.dto.FormProjection;
import com.mesh_suite.exception.BadRequestException;
import com.mesh_suite.exception.FormNotFoundException;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.service.notify.EmailService;
import com.mesh_suite.service.notify.NotificationMessageService;
import com.mesh_suite.util.FormUtils;
import com.mesh_suite.util.TimelineFilter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class FormsService {
    @Autowired
    private FormsRepository formsRepository;
    @Autowired
    private FormSectionRepository formSectionRepository;
    @Autowired
    private FormFieldRepository formFieldRepository;
    @Autowired
    private FormDataRepository formDataRepository;

    @Autowired
    private NotificationMessageService notificationMessageService;

    @Autowired
    private BillService billService;

    @Autowired
    private UserCompanyRepository userCompanyRepository;
    @Autowired
    private EmailService emailService;

    public Long createForm(Forms form) {
        if (form.getFormSections() != null) {
            form.getFormSections().forEach(section -> {
                section.setForm(form);

                // Ensure formFields is initialized
                if (section.getFormFields() != null) {
                    section.getFormFields().forEach(field -> {
                        field.setFormSection(section);

                        // Set default values for form fields if necessary
                        if (field.getChoiceValue() == null) field.setChoiceValue(new ArrayList<>());
                    });
                }
            });
        }

        Forms savedForm = formsRepository.save(form);
        return savedForm.getId();
    }
    @Transactional
    public Long duplicateForm(Long formId) {
        Forms originalForm = formsRepository.findById(formId)
                .orElseThrow(() -> new FormNotFoundException("Form not found with id: " + formId));

        // Copy the original form
        Forms duplicatedForm = new Forms();
        duplicatedForm.setName(originalForm.getName() + "_Copy");
        duplicatedForm.setCompanyId(0L);
        duplicatedForm.setUrl(""); // Reset URL for duplication
        duplicatedForm.setDescription(originalForm.getDescription());
        duplicatedForm.setFormInstruction(originalForm.getFormInstruction());
        duplicatedForm.setUserMandatory(originalForm.getUserMandatory());
        duplicatedForm.setDeadline(originalForm.getDeadline());
        duplicatedForm.setLayout(originalForm.getLayout());
        duplicatedForm.setPublishStatus(FormUtils.PublishStatus.DRAFT);
        duplicatedForm.setIsAnonymous(originalForm.getIsAnonymous());
        duplicatedForm.setRedirectUrl(originalForm.getRedirectUrl());
        duplicatedForm.setMultipleForms(originalForm.getMultipleForms());

        // Copy form sections and form fields
        for (FormSections originalSection : originalForm.getFormSections()) {
            FormSections duplicatedSection = new FormSections();
            duplicatedSection.setName(originalSection.getName());
            duplicatedSection.setDescription(originalSection.getDescription());
            duplicatedSection.setInstruction(originalSection.getInstruction());
            duplicatedSection.setOrdering(originalSection.getOrdering());
            duplicatedSection.setIsDeleted(false);

            // Set the reference to the duplicated form
            duplicatedSection.setForm(duplicatedForm);

            // Copy form fields
            for (FormField originalField : originalSection.getFormFields()) {
                FormField duplicatedField = new FormField();
                duplicatedField.setName(originalField.getName());
                duplicatedField.setDescription(originalField.getDescription());
                duplicatedField.setLabel(originalField.getLabel());
                duplicatedField.setPlaceHolder(originalField.getPlaceHolder());
                duplicatedField.setInstruction(originalField.getInstruction());
                duplicatedField.setOrdering(originalField.getOrdering());
                duplicatedField.setIsDeleted(false);
                duplicatedField.setFieldDataType(originalField.getFieldDataType());
                duplicatedField.setChoiceValue(new ArrayList<>(originalField.getChoiceValue()));
                duplicatedField.setIsMandatory(originalField.getIsMandatory());
                duplicatedField.setHorizontalAlign(originalField.getHorizontalAlign());
                duplicatedField.setValidPattern(originalField.getValidPattern());
                duplicatedField.setIsStatisticalField(originalField.getIsStatisticalField());
                duplicatedField.setStatisticalFunction(originalField.getStatisticalFunction());
                duplicatedField.setDisplayType(originalField.getDisplayType());
                duplicatedField.setMaxLength(originalField.getMaxLength());

                // Set the reference to the duplicated section
                duplicatedField.setFormSection(duplicatedSection);

                // Add the duplicated field to the duplicated section
                duplicatedSection.getFormFields().add(duplicatedField);
            }

            // Add the duplicated section to the duplicated form
            duplicatedForm.getFormSections().add(duplicatedSection);
        }

        // Save the duplicated form, which cascades to sections and fields
        return formsRepository.save(duplicatedForm).getId();
    }

    public Forms fetchFormById(Long formId) {
        Optional<Forms> optionalForm = formsRepository.findById(formId);
        return optionalForm.map(form -> {
            if (form.getIsDeleted()) {
                throw new FormNotFoundException("Form with ID " + formId + " is deleted.");
            } else {
                return form;
            }
        }).orElseThrow(() -> new FormNotFoundException("Form not found with ID: " + formId));
    }
    public Forms publishForm(Long formId) {
        Optional<Forms> optionalForm = formsRepository.findById(formId);

        return optionalForm.map(form -> {
            if (form.getPublishStatus() == FormUtils.PublishStatus.PUBLISHED) {
                throw new FormNotFoundException("Form with ID " + formId + " is already published.");
            } else if (form.getIsDeleted()) {
                throw new FormNotFoundException("Form with ID " + formId + " is deleted.");
            } else {
                form.setPublishStatus(FormUtils.PublishStatus.PUBLISHED);
                return formsRepository.save(form);
            }
        }).orElseThrow(() -> new FormNotFoundException("Form not found with ID: " + formId));
    }
    public Page<FormProjection> getForms(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size);

        return (timeline == null || timeline == Timeline.ALL)
                ? formsRepository.findAllNonDeletedNonTemplateForms(pageable)
                : formsRepository.findFilteredFormsByStartDate(TimelineFilter.calculateStartDate(timeline), pageable);
    }

    @Transactional
    public Forms updateForm(Forms updatedForm) {
        Forms existingForm = formsRepository.findById(updatedForm.getId())
                .orElseThrow(() -> new IllegalArgumentException("Form not found"));

        // Update fields of the existing form with the values from the updated form
        existingForm.setName(updatedForm.getName());
        existingForm.setCompanyId(updatedForm.getCompanyId());
        existingForm.setUrl(updatedForm.getUrl());
        existingForm.setDescription(updatedForm.getDescription());
        existingForm.setFormInstruction(updatedForm.getFormInstruction());
        existingForm.setUserMandatory(updatedForm.getUserMandatory());
        existingForm.setDeadline(updatedForm.getDeadline());
        existingForm.setPublishStatus(updatedForm.getPublishStatus());
        existingForm.setIsTemplate(updatedForm.getIsTemplate());
        existingForm.setIsDeleted(updatedForm.getIsDeleted());
        existingForm.setLayout(updatedForm.getLayout());
        existingForm.setUpdatedOn(LocalDateTime.now());
        existingForm.setIsAnonymous(updatedForm.getIsAnonymous());
        existingForm.setRedirectUrl(updatedForm.getRedirectUrl());
        existingForm.setMultipleForms(updatedForm.getMultipleForms());

        // Update or add form sections
        for (FormSections updatedSection : updatedForm.getFormSections()) {
            if (updatedSection.getId() == null) {
                // If section ID is null, treat it as a new section
                updatedSection.setForm(existingForm);
                formSectionRepository.save(updatedSection);
            } else {
                // If section ID exists, find the existing section and update it
                FormSections existingSection = formSectionRepository.findById(updatedSection.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Form section not found"));

                // Update fields of the existing form section with the values from the updated form section
                existingSection.setName(updatedSection.getName());
                existingSection.setDescription(updatedSection.getDescription());
                existingSection.setInstruction(updatedSection.getInstruction());
                existingSection.setOrdering(updatedSection.getOrdering());
                existingSection.setIsDeleted(updatedSection.getIsDeleted());

                // Update or add form fields within the section
                for (FormField updatedField : updatedSection.getFormFields()) {
                    if (updatedField.getId() == null) {
                        // If field ID is null, treat it as a new field
                        updatedField.setFormSection(existingSection);
                        formFieldRepository.save(updatedField);
                    } else {
                        // If field ID exists, find the existing field and update it
                        FormField existingField = formFieldRepository.findById(updatedField.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Form field not found"));

                        // Update fields of the existing form field with the values from the updated form field
                        existingField.setName(updatedField.getName());
                        existingField.setDescription(updatedField.getDescription());
                        existingField.setLabel(updatedField.getLabel());
                        existingField.setPlaceHolder(updatedField.getPlaceHolder());
                        existingField.setInstruction(updatedField.getInstruction());
                        existingField.setOrdering(updatedField.getOrdering());
                        existingForm.setIsDeleted(updatedField.getIsDeleted());
                        existingField.setFieldDataType(updatedField.getFieldDataType());
                        existingField.setChoiceValue(updatedField.getChoiceValue());
                        existingField.setIsMandatory(updatedField.getIsMandatory());
                        existingField.setHorizontalAlign(updatedField.getHorizontalAlign());
                        existingField.setValidPattern(updatedField.getValidPattern());
                        existingField.setIsStatisticalField(updatedField.getIsStatisticalField());
                        existingField.setStatisticalFunction(updatedField.getStatisticalFunction());
                        existingField.setDisplayType(updatedField.getDisplayType());
                        existingField.setMaxLength(updatedField.getMaxLength());

                        // Save the updated form field
                        formFieldRepository.save(existingField);
                    }
                }

                // Save the updated form section
                formSectionRepository.save(existingSection);
            }
        }

        // Save the updated form
        return formsRepository.save(existingForm);
    }
    public Forms getFormsById(Long formId) {
        return formsRepository.findById(formId)
                .orElseThrow(() -> new FormNotFoundException("Form not found with ID: " + formId));
    }
    public Forms getPublishedFormById(Long formId) {
        Forms form = formsRepository.findByIdAndPublishStatus(formId, FormUtils.PublishStatus.PUBLISHED)
                .orElseThrow(() -> new FormNotFoundException("Published form not found with ID: " + formId));

        if (form.getIsDeleted()) {
            throw new BadRequestException("Published form with ID " + formId + " is deleted");
        }

        return form;
    }
    public String deleteFormPermanently(Long formId) {
        Forms form = formsRepository.findById(formId)
                .orElseThrow(() -> new FormNotFoundException("Form with ID " + formId + " not found"));
        if (form.getPublishStatus() == FormUtils.PublishStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot delete a form with status PUBLISHED");
        }

        formsRepository.delete(form);
        return "Form deleted successfully";
    }

    public String softDeleteForm(Long formId) {
        Optional<Forms> optionalForm = formsRepository.findById(formId);
        return optionalForm.map(form -> {
            if (form.getIsDeleted()) {
                return "Form with id " + formId + " is already soft deleted.";
            } else if (form.getPublishStatus() != FormUtils.PublishStatus.PUBLISHED) {
                form.setIsDeleted(true);
                form.setDeletedOn(LocalDateTime.now());
                form.setPublishStatus(FormUtils.PublishStatus.DRAFT);
                formsRepository.save(form);
                return "Form deleted successfully.";
            } else {
                throw new RuntimeException("Cannot delete a form that has been published.");
            }
        }).orElseThrow(() -> new FormNotFoundException("Form not found with id: " + formId));
    }


    @Transactional
    public String undeleteForm(Long formId) {
        Optional<Forms> optionalForm = formsRepository.findById(formId);
        return optionalForm.map(form -> {
            if (form.getIsDeleted()) {
                form.setIsDeleted(false);
                form.setDeletedOn(LocalDateTime.now());
                formsRepository.save(form);
                return "Form undeleted successfully.";
            } else {
                return "Form with id " + formId + " is not deleted.";
            }
        }).orElse("Form not found with id: " + formId);
    }

    public String unpublishFormById(Long formId) {
        return formsRepository.findByIdAndIsDeletedFalse(formId)
                .map(form -> {
                    if (form.getPublishStatus() == FormUtils.PublishStatus.PUBLISHED) {
                        form.setPublishStatus(FormUtils.PublishStatus.UNPUBLISHED);
                        formsRepository.save(form);
                        return "Form unpublished successfully";
                    } else if (form.getPublishStatus() == FormUtils.PublishStatus.DRAFT) {
                        throw new IllegalStateException("Form with ID " + formId + " is in DRAFT status and cannot be unpublished");
                    } else {
                        throw new IllegalStateException("Form with ID " + formId + " is not currently published");
                    }
                })
                .orElseThrow(() -> new FormNotFoundException("Form not found with ID: " + formId));
    }

    public String unpublishFormByName(String formName) {
        Forms form = formsRepository.findByName(formName)
                .orElseThrow(() -> new FormNotFoundException("Form not found with name: " + formName));

        if (form.getPublishStatus() == FormUtils.PublishStatus.PUBLISHED) {
            form.setPublishStatus(FormUtils.PublishStatus.UNPUBLISHED);
            formsRepository.save(form);
            return "Form unpublished successfully";
        } else if (form.getPublishStatus() == FormUtils.PublishStatus.DRAFT) {
            throw new IllegalStateException("Form is in DRAFT status and cannot be unpublished");
        } else {
            throw new IllegalStateException("Form is not currently published");
        }
    }

    public Page<FormProjection> getRecentForms(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        return (timeline == null || timeline == Timeline.ALL)
                ? formsRepository.findAllRecentForms(pageable)
                : formsRepository.findAllRecentFormsWithTimeline(TimelineFilter.calculateStartDate(timeline), pageable);
    }

    public String renameForm(Long formId, String newName) {
        Forms form = formsRepository.findById(formId)
                .orElseThrow(() -> new FormNotFoundException("Form not found with ID: " + formId));

        if (form.getIsDeleted()) {
            return "Unable to rename form. Form with ID " + formId + " was soft deleted.";
        }

        form.setName(newName);
        form.setUpdatedOn(LocalDateTime.now());
        formsRepository.save(form);

        return "Form renamed successfully";
    }
    @Transactional
    public String assignCompanyIdToForm(Long formId, Long companyId) {
        log.info("Assigning company ID {} to form ID {}", companyId, formId);

        //  Fetch form
        Forms form = formsRepository.findByIdAndIsDeletedFalse(formId)
                .orElseThrow(() -> {
                    log.error("Form not found with ID: {}", formId);
                    return new FormNotFoundException("Form not found with ID: " + formId);
                });

        //  Assign company to the form
        form.setCompanyId(companyId);
        form.setAssignDate(LocalDateTime.now());
        formsRepository.save(form);
        log.info("Company ID {} successfully assigned to form ID {}", companyId, formId);

        boolean emailSent = false;

        // Send notification email
        try {
            UserCompany company = userCompanyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with ID " + companyId));

            Users companyAdmin = company.getCompanyAdmin();
            if (companyAdmin == null) {
                log.warn("No company admin found for company ID {}", companyId);
            } else {
                String assignDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


                emailService.sendFormAssignmentEmail(
                        companyAdmin.getFirstName(),
                        form.getName(),
                        assignDate,
                        companyAdmin.getEmail()
                );

                emailSent = true;
                log.info("Form assignment email sent to {} for form '{}'", companyAdmin.getEmail(), form.getName());
            }

        } catch (Exception ex) {
            log.error("Error sending assignment email for form ID {} and company ID {}: {}",
                    formId, companyId, ex.getMessage(), ex);
        }

        String message = emailSent
                ? "Company assigned to form and email sent successfully."
                : "Company assigned to form, but email sending failed.";

        log.info(message);
        return message;
    }


    public Page<Forms> getFormsByCompanyId(Long companyId, int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending().and(Sort.by("id")));

        Page<Forms> forms = (timeline == null || timeline == Timeline.ALL)
                ? formsRepository.findByCompanyId(companyId, pageable)
                : formsRepository.findByCompanyIdAndCreatedOnAfter(companyId, TimelineFilter.calculateStartDate(timeline), pageable);

        return forms;
    }
    public Page<FormProjection> findAllTemplatesWithAndFilter(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size);
        return (timeline == null || timeline == Timeline.ALL)
                ? formsRepository.findAllTemplatesNonDeleted(pageable)
                : formsRepository.findAllTemplatesNonDeletedWithTimeline(TimelineFilter.calculateStartDate(timeline), pageable);
    }
    public LocalDateTime getStartDateFromTimeline(Timeline timeline) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate date = now.toLocalDate();

        switch (timeline) {
            case TODAY:
                return now.toLocalDate().atStartOfDay();
            case THIS_WEEK:
                //start of the week (Monday)
                LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return startOfWeek.atStartOfDay();
            case THIS_MONTH:
                return date.withDayOfMonth(1).atStartOfDay();
            case THIS_YEAR:
                return date.withDayOfYear(1).atStartOfDay();
            case ALL:
                return null;
            default:
                throw new IllegalArgumentException("Unknown timeline: " + timeline);
        }
    }
    public long countPublishedForms() {
        return formsRepository.countByPublishStatusAndIsDeletedFalse(FormUtils.PublishStatus.PUBLISHED);
    }

    public long countUnpublishedOrDraftForms() {
        List<FormUtils.PublishStatus> statuses = List.of(
                FormUtils.PublishStatus.UNPUBLISHED,
                FormUtils.PublishStatus.DRAFT
        );
        return formsRepository.countByPublishStatusInAndIsDeletedFalse(statuses);
    }


    public Page<Forms> findCompletedFormsByUser(List<Long> completedFormIds,int page, int size, Timeline timeline) {

        LocalDateTime startDate = getStartDateFromTimeline(timeline);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        // Apply timeline filter if startDate is not null
        if (startDate != null && !completedFormIds.isEmpty()) {
            return formsRepository.findAllByIdAndCreatedOnAfter(completedFormIds, startDate, pageable);
        } else if (!completedFormIds.isEmpty()) {
            return formsRepository.findAllById(completedFormIds, pageable);
        } else {
            return Page.empty(pageable);
        }
    }
    public Page<Forms> findUnCompletedFormsByUser(List<Long> unCompletedFormIds,int page, int size, Timeline timeline) {
        LocalDateTime startDate = getStartDateFromTimeline(timeline);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        if (startDate != null && !unCompletedFormIds.isEmpty()) {
            return formsRepository.findAllByIdAndCreatedOnAfter(unCompletedFormIds, startDate, pageable);
        } else if (!unCompletedFormIds.isEmpty()) {
            return formsRepository.findAllById(unCompletedFormIds, pageable);
        } else {
            return Page.empty(pageable);
        }
    }

    public Map<String, Long> userFormStatistics(Long userId) {
        Map<String, Long> formCounts = new HashMap<>();

        // Count completed forms
        Long completedCount = formDataRepository.countByUserIdAndIsCompletedTrue(userId);
        formCounts.put("completedForms", completedCount);

        // Count uncompleted forms
        Long uncompletedCount = formDataRepository.countByUserIdAndIsCompletedFalse(userId);
        formCounts.put("uncompletedForms", uncompletedCount);

        return formCounts;
    }

    public List<Forms> getFormsByFormIds(List<Long> formIds) {
        return formsRepository.findByIdIn(formIds);
    }
    public String findNameByFormId(Long formId) {
        return formsRepository.findNameByFormId(formId)
                .orElseThrow(() -> new FormNotFoundException("Form with id " + formId + " not found"));
    }
    @Transactional
    public List<String> getChoiceValuesByFormId(Long formId) {
        return formFieldRepository.findChoiceValuesByFormId(formId);
    }

    public Page<Forms> getFormsWhereCompanyIdIsNull(int page, int size, Timeline timeline) {
        LocalDateTime startDate = getStartDateFromTimeline(timeline);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        if (startDate != null) {
            return formsRepository.findUnAssignFormsWithTimeline(startDate, pageable);
        } else {
            return formsRepository.findUnAssignForms(pageable);
        }
    }
    public Page<FormProjection> searchFormsByCompanyId(Long companyId, int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        return (timeline == null || timeline == Timeline.ALL)
                ? formsRepository.findByCompanyIdAndNotDeleted(companyId, pageable)
                : formsRepository.findByCompanyIdAndCreatedOnAfterAndNotDeleted(
                companyId,
                TimelineFilter.calculateStartDate(timeline),
                pageable);
    }


    public List<Long> getPublishedFormIds(FormUtils.PublishStatus publishStatus, Long companyId) {
        return formsRepository.findPublishedFormIds(publishStatus, companyId);
    }
    public void deleteFormSection(Long id) {
        formSectionRepository.findById(id).ifPresent(formSection -> {
            formSectionRepository.delete(formSection);
        });
    }

    public String getFormNameById(Long id) {
        log.info("Retrieving form name for ID: {}", id);
        return formsRepository.findNameById(id)
                .map(formName -> {
                    log.info("Form name found: {}", formName);
                    return formName;
                })
                .orElseThrow(() -> {
                    log.warn("No form found with ID: {}", id);
                    return new FormNotFoundException("No form found with ID: " + id);
                });
    }

    public Page<Forms> getFormsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        return formsRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    @Transactional
    public void updateSectionOrdering(List<FormElementOrderingDto> updates) {
        Map<Long, FormSections> sectionsMap = formSectionRepository.findAllById(
                        updates.stream().map(FormElementOrderingDto::getId).collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(FormSections::getId, Function.identity()));

        // Verify all sections were found
        if (sectionsMap.size() != updates.size()) {
            List<Long> missingIds = updates.stream()
                    .map(FormElementOrderingDto::getId)
                    .filter(id -> !sectionsMap.containsKey(id))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException("Form sections not found: " + missingIds);
        }

        // Update ordering in memory
        updates.forEach(update -> {
            FormSections section = sectionsMap.get(update.getId());
            section.setOrdering(update.getOrdering());
        });

        // Save all changes in one batch
        formSectionRepository.saveAll(sectionsMap.values());
    }

    @Transactional
    public void updateFormFieldOrdering(List<FormElementOrderingDto> updates) {
        // Get all fields in one query
        Map<Long, FormField> fieldsMap = formFieldRepository.findAllByIdIn(
                        updates.stream().map(FormElementOrderingDto::getId).collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(FormField::getId, Function.identity()));

        // Verify all fields were found
        if (fieldsMap.size() != updates.size()) {
            List<Long> missingIds = updates.stream()
                    .map(FormElementOrderingDto::getId)
                    .filter(id -> !fieldsMap.containsKey(id))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException("Form fields not found: " + missingIds);
        }

        // Additional validation - ensure all fields belong to the same section
        Long sectionId = null;
        for (FormField field : fieldsMap.values()) {
            if (sectionId == null) {
                sectionId = field.getFormSection().getId();
            } else if (!sectionId.equals(field.getFormSection().getId())) {
                throw new IllegalArgumentException("All fields must belong to the same section");
            }
        }

        // Update ordering in memory
        updates.forEach(update -> {
            FormField field = fieldsMap.get(update.getId());
            field.setOrdering(update.getOrdering());
        });

        // Save all changes in one batch
        formFieldRepository.saveAll(fieldsMap.values());
    }

}
