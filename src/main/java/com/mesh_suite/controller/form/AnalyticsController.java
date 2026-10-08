package com.mesh_suite.controller.form;

import com.mesh_suite.dto.response.AnalyticsPayload;
import com.mesh_suite.service.form.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mesh-suite/v1.0/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Platform analytics for businesses, programs, and clients")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "General business totals and demographic breakdowns")
    @GetMapping("/general-business")
    public ResponseEntity<AnalyticsPayload> generalBusiness() {
        return ResponseEntity.ok(analyticsService.generalBusiness());
    }

    @Operation(summary = "Loan and grant beneficiary totals for a program")
    @GetMapping("/loans-grants")
    public ResponseEntity<AnalyticsPayload> loansAndGrants(
            @RequestParam(defaultValue = "all") String programId) {
        return ResponseEntity.ok(analyticsService.loansAndGrants(programId));
    }

    @Operation(summary = "Training trainee totals for a program")
    @GetMapping("/training")
    public ResponseEntity<AnalyticsPayload> training(
            @RequestParam(defaultValue = "all") String programId) {
        return ResponseEntity.ok(analyticsService.training(programId));
    }

    @Operation(summary = "Client totals, activity, and month-by-month growth")
    @GetMapping("/clients")
    public ResponseEntity<AnalyticsPayload> clients() {
        return ResponseEntity.ok(analyticsService.clients());
    }
}
