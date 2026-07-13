package com.mesh_suite.controller.form;

import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.domain.form.FormField;
import com.mesh_suite.domain.form.Forms;
import com.mesh_suite.dto.*;
import com.mesh_suite.exception.BadRequestException;
import com.mesh_suite.exception.FormNotFoundException;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.service.form.FormDataService;
import com.mesh_suite.service.form.FormFieldService;
import com.mesh_suite.service.form.FormsService;
import com.mesh_suite.util.FormUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mesh-suite/v1.0/forms/builder")
@Tag(name = "Forms Builder API", description = "Forms Setup Related Operations")
public class FormsController {
    @Autowired
    private FormsService formService;

    @Autowired
    private FormFieldService formFieldService;

    @Autowired
    private FormDataService formDataService;


    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Create a new Form")
    public ResponseEntity<Long> createForm(@RequestBody Forms form) {
        return ResponseEntity.ok().body(formService.createForm(form));
    }

    @PostMapping("/duplicateForm/{id}")
    @Operation(summary = "Duplicates a Form")
    public ResponseEntity<Long> duplicateForm(@PathVariable(name = "id") Long id) {
        Long duplicatedFormId = formService.duplicateForm(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(duplicatedFormId);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Retrieves Form by id")
    public ResponseEntity<?> getFormById(@PathVariable(name = "id") Long id) {
        return new ResponseEntity<>(formService.getFormsById(id), HttpStatus.OK);
    }
    @GetMapping("/access-published-form/{formId}")
    @Operation(summary = "Retrieve Published Form by ID")
    public ResponseEntity<?> getPublishedFormById(@Parameter(description = "ID of the published form to retrieve") @PathVariable("formId") Long formId) {
        try {
            Forms form = formService.getPublishedFormById(formId);
            return ResponseEntity.ok(form);
        } catch (FormNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PutMapping("/update")
    @Operation(summary = "Update an Existing form and its associated element(form-section and form field)")
    public ResponseEntity<?> updateForm(@RequestBody Forms updateFormRequest) {
        Long formId = updateFormRequest.getId();
        if (formId == null || formId <= 0) {
            return ResponseEntity.badRequest().body("Form id is required and must be a positive non-zero value.");
        }
        return new ResponseEntity<>(formService.updateForm(updateFormRequest), HttpStatus.OK);
    }

    @GetMapping("/all/{page_number}/{page_size}/{timeline}")
    @Operation(summary = "Retrieve all Existing forms with optional timeline filter")
    public ResponseEntity<Paginate<FormProjection>> getForms(
            @Parameter(description = "Page number (zero-based index)") @PathVariable(name = "page_number") Integer pageNumber,
            @Parameter(description = "Page size") @PathVariable(name = "page_size") Integer pageSize,
            @Parameter(description = "Timeline filter") @PathVariable(name = "timeline") Timeline timeline) {

        Page<FormProjection> formsPage = formService.getForms(pageNumber - 1, pageSize, timeline);

        Paginate<FormProjection> response = new Paginate<>(
                pageNumber,
                pageSize,
                formsPage.getTotalElements(),
                formsPage.getTotalPages(),
                formsPage.getContent()
        );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete Form permanently from the system by id")
    public ResponseEntity<String> deleteFormPermanently(@PathVariable Long id) {
        String message = formService.deleteFormPermanently(id);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/soft-delete/{id}")
    @Operation(summary = "Soft Deletion of Form")
    public ResponseEntity<String> softDeleteForm(@PathVariable(name = "id") Long formId) {
        String message;
        try {
            message = formService.softDeleteForm(formId);
            return ResponseEntity.ok(message);
        } catch (FormNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/undelete/{id}")
    @Operation(summary = "UnDelete of Form")
    public ResponseEntity<String> unDeleteForm(@PathVariable(name="id") Long id) {
        return ResponseEntity.ok(formService.undeleteForm(id));
    }

    @GetMapping("/recent/{page}/{size}/{timeline}")
    @Operation(summary = "Retrieve recent forms with pagination and optional timeline filter")
    public ResponseEntity<Paginate<FormProjection>> getRecentForms(
            @Parameter(description = "Page number (zero-based index)") @PathVariable(name = "page") int page,
            @Parameter(description = "Page size") @PathVariable(name = "size") int size,
            @Parameter(description = "Timeline filter") @PathVariable(name = "timeline") Timeline timeline) {

        Page<FormProjection> formsPage = formService.getRecentForms(page, size, timeline);

        Paginate<FormProjection> response = new Paginate<>(
                page,
                size,
                formsPage.getTotalElements(),
                formsPage.getTotalPages(),
                formsPage.getContent()
        );

        return ResponseEntity.ok(response);
    }



    @PutMapping("/rename/{formId}")
    @Operation(summary = "Rename a form")
    public ResponseEntity<String> renameForm(@PathVariable(name = "formId") Long formId, @RequestBody String newName) {
        try {
            String message = formService.renameForm(formId, newName);
            return ResponseEntity.ok(message);
        } catch (FormNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }
    @PutMapping("/publish/{formId}")
    @Operation(summary = "Published form by form Id" )
    public ResponseEntity<?> publishFormById(@PathVariable(name = "formId") Long formId) {
        try {
            Forms publishedForm = formService.publishForm(formId);
            return ResponseEntity.ok(publishedForm);
        } catch (FormNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PutMapping("/unpublish/by-id/{formId}")
    @Operation(summary = "Unpublish a form by ID")
    public ResponseEntity<String> unpublishFormById(@PathVariable(name ="formId") Long formId) {
        return ResponseEntity.ok(formService.unpublishFormById(formId));
    }

    @PutMapping("/unpublish/by-name/{formName}")
    @Operation(summary = "Unpublish a form by form name")
    public ResponseEntity<String> unpublishFormByName(@PathVariable(name ="formName") String formName) {
        return ResponseEntity.ok(formService.unpublishFormByName(formName));
    }

    @PutMapping("/company/{formId}/{companyId}")
    @Operation(summary = "Assign Company to Forms")
    public ResponseEntity<String> assignCompanyToForm(@PathVariable(name = "formId") Long formId, @PathVariable(name = "companyId") Long companyId) {
        return ResponseEntity.ok(formService.assignCompanyIdToForm(formId,companyId));
    }

    @GetMapping("/company/{companyId}/{page}/{size}/{timeline}")
    @Operation(summary = "Retrieve Forms by Company Id with optional timeline filter")
    public ResponseEntity<PaginatedFormsResponse> getFormsByCompanyId(
            @PathVariable(name = "companyId") Long companyId,
            @PathVariable(name = "page") int page,
            @PathVariable(name = "size") int size,
            @PathVariable(name = "timeline") Optional<Timeline> timeline) {

        Page<Forms> formsPage = formService.getFormsByCompanyId(companyId, page, size, timeline.orElse(null));
        PaginatedFormsResponse response = new PaginatedFormsResponse();
        response.setFirst(formsPage.isFirst());
        response.setLast(formsPage.isLast());
        response.setTotalElements(formsPage.getTotalElements());
        response.setTotalPages(formsPage.getTotalPages());
        response.setSize(formsPage.getSize());
        response.setContent(formsPage.getContent());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/list-templates/{page_number}/{page_size}/{timeline}")
    @Operation(summary = "List available templates with optional timeline filter")
    public ResponseEntity<Paginate<FormProjection>> listExistingTemplates(
            @Parameter(description = "Page number") @PathVariable(name = "page_number") Integer pageNumber,
            @Parameter(description = "Page size") @PathVariable(name = "page_size") Integer pageSize,
            @Parameter(description = "Timeline filter") @PathVariable(name = "timeline") Timeline timeline) {

        Page<FormProjection> templatesPage = formService.findAllTemplatesWithAndFilter(pageNumber - 1, pageSize, timeline);

        Paginate<FormProjection> response = new Paginate<>(
                pageNumber,
                pageSize,
                templatesPage.getTotalElements(),
                templatesPage.getTotalPages(),
                templatesPage.getContent()
        );

        return ResponseEntity.ok(response);
    }
    @PutMapping("/field-update")
    @Operation(summary = "Update form field")
    public ResponseEntity<?> updateFormField(@RequestBody FormFieldDto updatedFormField) {
        try {
            FormField updatedField = formFieldService.updateFormField(updatedFormField);
            return ResponseEntity.ok(updatedField);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/user/completed-forms/{page}/{size}/{timeline}/{completedFormIds}")
    @Operation(summary = "Find completed forms by list of completed form response ids with pagination and timeline filter")
    public ResponseEntity<PaginatedFormsResponse> findCompletedFormsByUser(
            @Parameter(description = "Page number (zero-based index)") @PathVariable(name = "page") int page,
            @Parameter(description = "Page size") @PathVariable(name = "size") int size,
            @Parameter(description = "Timeline filter") @PathVariable(name = "timeline") Timeline timeline,
            @Parameter(description = "Comma-separated list of completed form IDs") @PathVariable("completedFormIds") String completedFormIds) {

        // Convert comma-separated IDs string to List<Long>
        List<Long> completedFormIdsList = Arrays.stream(completedFormIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        Page<Forms> formsPage = formService.findCompletedFormsByUser(completedFormIdsList,page, size, timeline);

        PaginatedFormsResponse response = new PaginatedFormsResponse();
        response.setFirst(formsPage.isFirst());
        response.setLast(formsPage.isLast());
        response.setTotalElements(formsPage.getTotalElements());
        response.setTotalPages(formsPage.getTotalPages());
        response.setSize(formsPage.getSize());
        response.setContent(formsPage.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/uncompleted-forms/{page}/{size}/{timeline}/{unCompletedFormIds}")
    @Operation(summary = "Find list of uncompleted form response by ids with pagination and timeline filter")
    public ResponseEntity<PaginatedFormsResponse> findUnCompletedFormsByUser(
            @Parameter(description = "Page number (zero-based index)") @PathVariable(name = "page") int page,
            @Parameter(description = "Page size") @PathVariable(name = "size") int size,
            @Parameter(description = "Timeline filter") @PathVariable(name = "timeline") Timeline timeline,
            @Parameter(description = "Comma-separated list of uncompleted form IDs") @PathVariable("unCompletedFormIds") String unCompletedFormIds) {

        // Convert comma-separated IDs string to List<Long>
        List<Long> unCompletedFormIdsList = Arrays.stream(unCompletedFormIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        Page<Forms> formsPage = formService.findUnCompletedFormsByUser(unCompletedFormIdsList,page, size, timeline);

        PaginatedFormsResponse response = new PaginatedFormsResponse();
        response.setFirst(formsPage.isFirst());
        response.setLast(formsPage.isLast());
        response.setTotalElements(formsPage.getTotalElements());
        response.setTotalPages(formsPage.getTotalPages());
        response.setSize(formsPage.getSize());
        response.setContent(formsPage.getContent());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/user/form-statistics/{userId}")
    @Operation(summary = "Count completed and uncompleted forms by user ID", description = "Get the counts of completed and uncompleted forms associated with the specified user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved form counts"),
            @ApiResponse(responseCode = "404", description = "No forms found for the given user ID")
    })
    public Map<String, Long> userFormStatistics(
            @Parameter(description = "User ID of the forms owner", required = true)
            @PathVariable("userId") Long userId) {
        return formService.userFormStatistics(userId);
    }
   @GetMapping("/user-forms-by/{formIds}")
   @Operation(summary = "Get all forms by list of form IDs")
   public ResponseEntity<List<Forms>> getFormsByFormIds(
           @Parameter(description = "Comma-separated list of form IDs") @PathVariable String formIds) {

       List<Long> formIdList = Arrays.stream(formIds.split(","))
               .map(Long::parseLong)
               .distinct()
               .collect(Collectors.toList());

       List<Forms> forms = formService.getFormsByFormIds(formIdList);
       return ResponseEntity.ok(forms);
   }
    @GetMapping("/unassigned-forms/{page}/{size}/{timeline}")
    @Operation(summary = "List unassigned forms with pagination and optional timeline filter", description = "Get all unassigned forms with pagination and optional timeline filter.")
    public ResponseEntity<PaginatedFormsResponse> getUnAssignForms(
            @Parameter(description = "Page number (zero-based index)") @PathVariable("page") int page,
            @Parameter(description = "Page size") @PathVariable("size") int size,
            @Parameter(description = "Timeline filter") @PathVariable("timeline") Timeline timeline) {

        Page<Forms> formsPage = formService.getFormsWhereCompanyIdIsNull(page, size, timeline);

        PaginatedFormsResponse response = new PaginatedFormsResponse();
        response.setFirst(formsPage.isFirst());
        response.setLast(formsPage.isLast());
        response.setTotalElements(formsPage.getTotalElements());
        response.setTotalPages(formsPage.getTotalPages());
        response.setSize(formsPage.getSize());
        response.setContent(formsPage.getContent());

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Delete a FormField by its ID")
    @DeleteMapping("/formfield/{id}")
    public ResponseEntity<Void> deleteFormField(@PathVariable(name = "id") Long id) {
        formFieldService.deleteFormFieldById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search-assign-forms/{companyId}/{page}/{size}/{timeline}")
    @Operation(summary = "Search assigned forms by company ID with pagination and optional timeline filter", description = "Search forms by their company ID with pagination and optional timeline filter.")
    public ResponseEntity<Paginate<FormProjection>> searchFormsByCompanyId(
            @PathVariable("companyId") Long companyId,
            @PathVariable("page") int page,
            @PathVariable("size") int size,
            @PathVariable("timeline") Timeline timeline) {

        Page<FormProjection> formsPage = formService.searchFormsByCompanyId(companyId, page, size, timeline);

        Paginate<FormProjection> response = new Paginate<>(
                page,
                size,
                formsPage.getTotalElements(),
                formsPage.getTotalPages(),
                formsPage.getContent()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/published-forms-ids/{companyId}")
    @Operation(
            summary = "Retrieve published form IDs by company ID",
            description = "Fetches a list of published form IDs based on the company ID."
    )
    public ResponseEntity<List<Long>> getPublishedFormIds(
            @Parameter(description = "Company ID", required = true) @PathVariable("companyId") Long companyId) {
        List<Long> publishedFormIds = formService.getPublishedFormIds(FormUtils.PublishStatus.PUBLISHED, companyId);

        if (publishedFormIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(publishedFormIds);
    }

    @Operation(summary = "Delete form section and associated form fields")
    @DeleteMapping("/form-section/{id}")
    public ResponseEntity<String> deleteFormSection(@PathVariable Long id) {
        formService.deleteFormSection(id);
        return ResponseEntity.ok("Form section and associated form fields deleted successfully.");
    }
    @PutMapping("/form-section-ordering")
    @Operation(summary = "Form section ordering updates")
    public ResponseEntity<Void> updateMultipleSectionOrdering(
            @Valid @RequestBody List<FormElementOrderingDto> updates) {

        formService.updateSectionOrdering(updates);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/form-field-ordering")
    @Operation(summary = "Form Field ordering updates")
    public ResponseEntity<Void> updateMultipleFieldOrdering(
            @Valid @RequestBody List<FormElementOrderingDto> updates) {

        formService.updateFormFieldOrdering(updates);
        return ResponseEntity.ok().build();
    }
}
