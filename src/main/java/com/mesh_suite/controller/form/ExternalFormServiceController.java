package com.mesh_suite.controller.form;

import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.domain.form.ApiKey;
import com.mesh_suite.domain.form.FormData;
import com.mesh_suite.domain.form.Forms;
import com.mesh_suite.dto.*;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.service.form.ApiKeyService;
import com.mesh_suite.service.form.FormDataService;
import com.mesh_suite.service.form.FormsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/mesh-suite/v1.0/external/forms-service")
@Tag(name = "External Forms Service API", description = "Forms Response Data")
@Slf4j
public class ExternalFormServiceController {
    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private FormDataService formDataService;
    @Autowired
    private FormsService formService;


    @PostMapping(
            value = "/forms/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Create a new Form")
    public ResponseEntity<Long> createForm(@RequestBody Forms form) {
        return ResponseEntity.ok().body(formService.createForm(form));
    }

    @GetMapping("/forms/{id}")
    @Operation(summary = "Retrieves Form by id")
    public ResponseEntity<?> getFormById(@PathVariable(name = "id") Long id) {
        return new ResponseEntity<>(formService.getFormsById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create Client Access Credential to be used to generate token")
    @PostMapping("/client")
    public ResponseEntity<?> registerClient(@RequestBody ApiKey apiKey) {
        try {
            return ResponseEntity.ok(apiKeyService.registerClient(apiKey));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding API Key: " + e.getMessage());
        }
    }
    @PostMapping("/token")
    public ResponseEntity<?> generateToken(@RequestBody TokenRequest tokenRequest) {
        try {
            String token = apiKeyService.generateToken(tokenRequest.getUsername(), tokenRequest.getPassword());
            return ResponseEntity.ok(token);
        }catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected Error: " + e.getMessage());
        }
    }
    @PostMapping("/collect-response")
    @Operation(
            summary = "Store Form Response Data",
            description = "Create User form data for any form setup."
    )
    public ResponseEntity<?> saveFormData(@RequestBody FormData formData) {
        try {
            return ResponseEntity.ok(formDataService.saveExternalFormData(formData));
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PutMapping("/update-response")
    @Operation(
            summary = "Update User Form Response Data",
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

    @GetMapping("/response-by-id/{id}")
    @Operation(summary = "Fetch single form response by response id")
    public ResponseEntity<FormData> getFormDataById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(formDataService.getFormDataById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/company-data/{companyId}/{page}/{size}/{timeline}")
    @Operation(
            summary = "Fetch all forms response data for a company using companyId")
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
    @GetMapping("/data/{formId}/{page}/{size}/{timeline}")
    @Operation(summary = "Fetch form responses for a form using form id with pagination and timeline filter")
    public ResponseEntity<Paginate<FormDataProjection>> getAllFormDataByFormId(
            @Parameter(description = "Form ID", required = true) @PathVariable(name = "formId") Long formId,
            @Parameter(description = "Page number (zero-based index)") @PathVariable(name = "page") int page,
            @Parameter(description = "Page size") @PathVariable(name = "size") int size,
            @Parameter(description = "Timeline filter") @PathVariable(name = "timeline") Timeline timeline) {
        try {
            Page<FormDataProjection> formDataPage = formDataService.getFormDataByFormId(formId, page, size, timeline);

            Paginate<FormDataProjection> response = new Paginate<>();
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

    @Operation(summary = "Fetches statistical results for a given form and company.")
    @GetMapping("/analytics/{formId}/{companyId}")
    public List<StatisticResult> getStatistics(@PathVariable Long formId, @PathVariable Long companyId) {
        return formDataService.getStatistics(formId, companyId);
    }

    @GetMapping("/company/{companyId}/{page}/{size}/{timeline}")
    @Operation(summary = "Fetch assigned Forms to Company by companyId with optional timeline filter")
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

    @GetMapping("/search-form-by/{name}/{page}/{size}")
    @Operation(summary = "Find forms by form name (case insensitive) with pagination")
    public ResponseEntity<PaginatedFormsResponse> getFormsByName(
            @PathVariable(name = "name") String name,
            @PathVariable(name = "page") int page,
            @PathVariable(name = "size") int size) {

        Page<Forms> formsPage = formService.getFormsByName(name, page, size);

        PaginatedFormsResponse response = new PaginatedFormsResponse();
        response.setFirst(formsPage.isFirst());
        response.setLast(formsPage.isLast());
        response.setTotalElements(formsPage.getTotalElements());
        response.setTotalPages(formsPage.getTotalPages());
        response.setSize(formsPage.getSize());
        response.setContent(formsPage.getContent());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/base-url")
    public String getBaseUrl(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" +
                request.getServerName() + ":" +
                request.getServerPort() +
                request.getContextPath();
        return baseUrl;
    }

    @PutMapping("/forms-builder/update")
    @Operation(summary = "Update an Existing form and its associated element(form-section and form field)")
    public ResponseEntity<?> updateForm(@RequestBody Forms updateFormRequest) {
        Long formId = updateFormRequest.getId();
        if (formId == null || formId <= 0) {
            return ResponseEntity.badRequest().body("Form id is required and must be a positive non-zero value.");
        }
        return new ResponseEntity<>(formService.updateForm(updateFormRequest), HttpStatus.OK);
    }
}