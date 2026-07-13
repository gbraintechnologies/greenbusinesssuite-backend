package com.mesh_suite.controller.form;

import com.mesh_suite.service.form.FormsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mesh-suite/v1.0/forms/dashboard")
@Tag(name = "Forms Dashboard API", description = "Forms Dashboard Data")
public class DashboardController {

    @Autowired
    private FormsService formsService;

    @GetMapping("/published-forms/count")
    @Operation(summary = "Number of Published Forms")
    public ResponseEntity<Long> countPublishedForms() {
        long count = formsService.countPublishedForms();
        return ResponseEntity.ok(count);
    }


    @GetMapping("/unpublished-forms/count")
    @Operation(summary = "Number of Unpublished or Draft Forms")
    public ResponseEntity<Long> countUnpublishedOrDraftForms() {
        long count = formsService.countUnpublishedOrDraftForms();
        return ResponseEntity.ok(count);
    }
}
