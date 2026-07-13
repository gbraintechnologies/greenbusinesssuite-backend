package com.mesh_suite.controller.form;

import com.mesh_suite.constant.forms.FormResponseStatus;
import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.domain.form.FormData;
import com.mesh_suite.dto.*;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.service.form.FormDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mesh-suite/v1.0/forms/response")
@Slf4j
@Tag(name = "Forms Response Data Service API", description = "Data collected through the form")
public class FormDataController {

    @Autowired
    private FormDataService formDataService;

    @PostMapping("/create")
    @Operation(
            summary = "Create User Form Response Data",
            description = "Create User  form data for any form setup."
    )
    public ResponseEntity<?> saveFormData(@RequestBody FormData formData) {
        try {
            return ResponseEntity.ok(formDataService.saveFormData(formData));
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(
            summary = "Update User Response Form Data",
            description = "Update User form data for any form setup."
    )
    public ResponseEntity<?> updateFormData(@RequestBody FormData formData) {
        try {
            FormData formsDataDto = formDataService.updateFormData(formData);
            return ResponseEntity.ok().body(formsDataDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{userId}/{formId}")
    @Operation(summary = "Delete User Form Data")
    public ResponseEntity<String> deleteFormDataByUserIdAndFormId(
            @PathVariable(name = "userId") Long userId,
            @PathVariable(name = "formId") Long formId) {
        try {
            formDataService.deleteFormDataByUserIdAndFormId(userId, formId);
            return ResponseEntity.ok("Form data deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete form data: ");
        }
    }

    @GetMapping("/user-data/{responseId}")
    @Operation(summary = "Retrieve form response data by response id")
    public ResponseEntity<FormData> getFormDataById(@PathVariable Long responseId) {
        try {
            return ResponseEntity.ok(formDataService.getFormDataById(responseId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/user-data/with-pay-details/{responseId}")
    @Operation(summary = "Retrieve form response data and payment details by response id")
    public ResponseEntity<Map<String, Object>> getFormDataWithPaymentById(@PathVariable Long responseId) {
        try {
            return ResponseEntity.ok(formDataService.getFormDataWithPaymentById(responseId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/all/{page}/{size}/{timeline}")
    @Operation(summary = "Retrieve all form responses with pagination and timeline filter")
    public ResponseEntity<FormDataResponse> getAllFormData(
            @Parameter(description = "Page number (zero-based index)") @PathVariable("page") int page,
            @Parameter(description = "Page size") @PathVariable("size") int size,
            @Parameter(description = "Timeline filter") @PathVariable("timeline") Timeline timeline) {

        Page<FormData> formDataPage = formDataService.getAllFormData(page, size, timeline);

        FormDataResponse response = new FormDataResponse();
        response.setFirst(formDataPage.isFirst());
        response.setLast(formDataPage.isLast());
        response.setTotalElements(formDataPage.getTotalElements());
        response.setTotalPages(formDataPage.getTotalPages());
        response.setSize(formDataPage.getSize());
        response.setContent(formDataPage.getContent());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete form response data by response ID")
    @DeleteMapping("/{responseId}")
    public ResponseEntity<Void> deleteFormData(@PathVariable Long responseId) {
        try {
            formDataService.deleteFormData(responseId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/data/{formId}/{page}/{size}/{timeline}")
    @Operation(summary = "Fetch form responses for a form using form id with pagination and timeline filter")
    public ResponseEntity<Paginate<FormDataWithPaymentStatus>> getAllFormDataByFormId(
            @Parameter(description = "Form ID", required = true) @PathVariable(name = "formId") Long formId,
            @Parameter(description = "Page number (zero-based index)") @PathVariable(name = "page") int page,
            @Parameter(description = "Page size") @PathVariable(name = "size") int size,
            @Parameter(description = "Timeline filter") @PathVariable(name = "timeline") Timeline timeline) {
        try {
            Page<FormDataWithPaymentStatus> formDataPage = formDataService.getAllFormDataByFormId(formId, page, size, timeline);

            Paginate<FormDataWithPaymentStatus> response = new Paginate<>();
            response.setPage(page);
            response.setSize(size);
            response.setTotalElements(formDataPage.getTotalElements());
            response.setTotalPages(formDataPage.getTotalPages());
            response.setContent(formDataPage.getContent());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/forms-status/stats")
    @Operation(
            summary = "Retrieve count of completed and not completed forms response data",
            description = "Retrieves the count of completed and not completed forms. Returns a map containing the counts.")
    public ResponseEntity<Map<String, Long>> getFormsDataCounts() {
        Map<String, Long> formsDataCounts = formDataService.getFormsDataCounts();
        return ResponseEntity.ok(formsDataCounts);
    }

    @GetMapping("/forms-status/count/{formId}")
    @Operation(
            summary = "Get Form Response Data Counts by Form ID",
            description = "Retrieves the count of completed and not completed forms by form ID. Accepts the form ID as a path variable. Returns a map containing the counts."

    )
    public ResponseEntity<Map<String, Long>> getCountsByFormId(@PathVariable(name = "formId") Long formId) {
        Map<String, Long> countsMap = formDataService.getCountsByFormId(formId);
        return ResponseEntity.ok(countsMap);
    }

    @GetMapping("/user-form/{userId}/{companyId}/{formId}")
    @Operation(
            summary = "Retrieve Form Data by User Information",
            description = "Retrieves form data based on user information provided(userId, company name and formId)"
    )
    public ResponseEntity<List<FormData>> getAllFormDataByFormDataRequest(
            @PathVariable Long userId,
            @PathVariable Long companyId,
            @PathVariable Long formId) {
        List<FormData> formDataList = formDataService.getAllFormDataByFormDataRequest(userId, companyId, formId);
        return new ResponseEntity<>(formDataList, HttpStatus.OK);
    }

    @GetMapping("/data/user-company/{userId}/{companyId}")
    @Operation(
            summary = "Retrieve Form Response Data by User ID and Company Id")
    public ResponseEntity<List<FormData>> getAllFormDataByUserIdAndCompanyName(
            @PathVariable("userId") Long userId,
            @PathVariable("companyId") Long companyId) {
        List<FormData> formDataList = formDataService.getAllFormDataByUserIdAndCompanyId(userId, companyId);
        return new ResponseEntity<>(formDataList, HttpStatus.OK);
    }

    @GetMapping("/company-data/{companyId}/{page}/{size}/{timeline}")
    @Operation(
            summary = "Retrieve Company Form Response Data by Company ID",
            description = "Retrieves form data based on the company ID provided in the request with pagination and optional timeline filter."
    )
    public ResponseEntity<FormDataResponse> getAllFormDataByCompanyId(
            @Parameter(description = "Company ID", required = true) @PathVariable("companyId") Long companyId,
            @Parameter(description = "Page number (zero-based index)") @PathVariable("page") int page,
            @Parameter(description = "Page size") @PathVariable("size") int size,
            @Parameter(description = "Timeline filter") @PathVariable("timeline") Timeline timeline) {

        Page<FormData> formDataPage = formDataService.getAllFormDataByCompanyId(companyId, page, size, timeline);

        FormDataResponse response = new FormDataResponse();
        response.setFirst(formDataPage.isFirst());
        response.setLast(formDataPage.isLast());
        response.setTotalElements(formDataPage.getTotalElements());
        response.setTotalPages(formDataPage.getTotalPages());
        response.setSize(formDataPage.getSize());
        response.setContent(formDataPage.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/{formId}/{companyId}")
    public List<StatisticResult> getStatistics(@PathVariable Long formId, @PathVariable Long companyId) {
        return formDataService.getStatistics(formId, companyId);
    }

    @Operation(summary = "Get count of ignored links for the specified company")
    @GetMapping("/ignored-links/{companyId}/{publishedFormIds}")
    public ResponseEntity<Long> getIgnoredLinks(
            @Parameter(description = "Company ID", required = true) @PathVariable("companyId") Long companyId,
            @Parameter(description = "List of published form IDs", required = true) @PathVariable("publishedFormIds") String publishedFormIds) {
        List<Long> formIdsList = Arrays.stream(publishedFormIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        long count = formDataService.countIgnoredLinks(companyId, formIdsList);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/opened-links/{companyId}/{publishedFormIds}")
    @Operation(
            summary = "Get count of opened links for the specified company",
            description = "Fetches the count of opened links based on the provided company ID and list of published form IDs."
    )
    public ResponseEntity<Long> getOpenedLinks(
            @Parameter(description = "Company ID", required = true) @PathVariable("companyId") Long companyId,
            @Parameter(description = "Comma-separated list of published form IDs", required = true) @PathVariable("publishedFormIds") String publishedFormIds) {

        List<Long> formIdsList = Arrays.stream(publishedFormIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        long count = formDataService.countLinksOpened(companyId, formIdsList);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Retrieve count of unique individuals who have filled a form belonging to the company")
    @GetMapping("/unique-users/count/{companyId}")
    public ResponseEntity<Long> countUniqueUserIds(@PathVariable(name = "companyId") Long companyId) {
        long uniqueUserCount = formDataService.countUniqueUserIds(companyId);
        return ResponseEntity.ok(uniqueUserCount);
    }

    @Operation(summary = "Retrieve total number of entries (sum of completed and incomplete submissions) for the company")
    @GetMapping("/total-entries/{companyId}")
    public ResponseEntity<Long> countTotalEntries(@PathVariable(name = "companyId") Long companyId) {
        long totalEntries = formDataService.countTotalEntries(companyId);
        return ResponseEntity.ok(totalEntries);
    }

    @Operation(summary = "Retrieve total number of completed and uncompleted forms by company name")
    @GetMapping("/total-forms/{companyId}")
    public ResponseEntity<Map<String, Long>> getTotalFormsByCompanyName(@PathVariable(name = "companyId") Long companyId) {
        Map<String, Long> formSummary = formDataService.getTotalFormsSummary(companyId);
        return ResponseEntity.ok(formSummary);
    }

    @Operation(summary = "Update the status of a form response")
    @PutMapping("/{status}/{id}")
    public ResponseEntity<?> updateStatus(
            @Parameter(description = "ID of the form response to be updated", required = true, in = ParameterIn.PATH)
            @PathVariable Long id,
            @Parameter(description = "New status of the form response", required = true, schema = @Schema(implementation = FormResponseStatus.class))
            @PathVariable FormResponseStatus status) {
        Optional<FormData> optionalFormData = formDataService.updateStatus(id, status);
        if (!optionalFormData.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalFormData.get());
    }

    @GetMapping("/completed-forms-count/{companyId}/{page}/{size}/{timeline}")
    @Operation(
            summary = "Get count of completed forms for each user by company",
            description = "Retrieves the count of completed forms for each user by company with pagination and optional timeline filtering."
    )
    public ResponseEntity<?> getCompletedFormCountsByCompanyId(
            @Parameter(description = "Company ID", required = true) @PathVariable Long companyId,
            @Parameter(description = "Page number (zero-based index)") @PathVariable int page,
            @Parameter(description = "Page size") @PathVariable int size,
            @Parameter(description = "Timeline filter") @PathVariable Timeline timeline) {

        log.info("Received request: companyId={}, page={}, size={}, timeline={}", companyId, page, size, timeline);
        if (size < 1) {
            log.warn("Invalid page size: {}", size);
            return ResponseEntity.badRequest().body("Page size must be at least 1.");
        }
        try {
            CompletedFormsCountResponse response = formDataService.getCompletedFormCountsByCompanyId(companyId, page, size, timeline);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching completed forms count: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }

    }

    @Operation(summary = "Get completed form responses by user ID")
    @GetMapping("/completed/{userId}")
    public ResponseEntity<List<FormData>> getCompletedForms(@PathVariable Long userId) {
        return ResponseEntity.ok(formDataService.getCompletedFormsByUserId(userId));
    }

    @Operation(summary = "Get uncompleted form responses by user ID")
    @GetMapping("/uncompleted/{userId}")
    public ResponseEntity<List<FormData>> getUncompletedForms(@PathVariable Long userId) {
        return ResponseEntity.ok(formDataService.getUncompletedFormsByUserId(userId));
    }

    @GetMapping("/form-processing-status/{companyId}/{status}/{page}/{size}")
    @Operation(summary = "Find form processing status by company ID and status", description = "Get all completed form data associated with the specified company ID and status with pagination.")
    public ResponseEntity<FormDataResponse> findCompletedFormDataByCompanyAndStatus(
            @Parameter(description = "Company ID", required = true) @PathVariable("companyId") Long companyId,
            @Parameter(description = "Form response status", required = true) @PathVariable("status") FormResponseStatus status,
            @Parameter(description = "Page number (zero-based index)") @PathVariable(name = "page") int page,
            @Parameter(description = "Page size") @PathVariable(name = "size") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<FormData> completedFormDataPage = formDataService.findCompletedFormDataByCompanyAndStatus(companyId, status, pageable);

        FormDataResponse response = new FormDataResponse();
        response.setFirst(completedFormDataPage.isFirst());
        response.setLast(completedFormDataPage.isLast());
        response.setTotalElements(completedFormDataPage.getTotalElements());
        response.setTotalPages(completedFormDataPage.getTotalPages());
        response.setSize(completedFormDataPage.getSize());
        response.setContent(completedFormDataPage.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all form response data associated with a specific user ID.")
    public ResponseEntity<List<FormData>> getFormsByUserId(@PathVariable Long userId) {
        List<FormData> forms = formDataService.getFormsByUserId(userId);
        return ResponseEntity.ok(forms);
    }


}
