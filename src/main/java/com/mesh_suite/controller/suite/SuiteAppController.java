package com.mesh_suite.controller.suite;

import com.mesh_suite.domain.suite.SuiteApp;
import com.mesh_suite.dto.request.SuiteAppRequest;
import com.mesh_suite.service.suite.SuiteAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mesh-suite/v1.0/suite-apps")
@RequiredArgsConstructor
@Tag(name = "Suite Apps", description = "Published Green Business Suite apps catalog")
public class SuiteAppController {

    private final SuiteAppService suiteAppService;

    @GetMapping("/published")
    @Operation(summary = "List published suite apps (public)")
    public ResponseEntity<List<SuiteApp>> listPublished() {
        return ResponseEntity.ok(suiteAppService.listPublished());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('APEX','SUPERADMIN')")
    @Operation(summary = "List all suite apps (admin)")
    public ResponseEntity<List<SuiteApp>> listAll() {
        return ResponseEntity.ok(suiteAppService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('APEX','SUPERADMIN')")
    public ResponseEntity<SuiteApp> getById(@PathVariable Long id) {
        return ResponseEntity.ok(suiteAppService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('APEX','SUPERADMIN')")
    public ResponseEntity<SuiteApp> create(@Valid @RequestBody SuiteAppRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(suiteAppService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('APEX','SUPERADMIN')")
    public ResponseEntity<SuiteApp> update(
            @PathVariable Long id,
            @Valid @RequestBody SuiteAppRequest request) {
        return ResponseEntity.ok(suiteAppService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('APEX','SUPERADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        suiteAppService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
