package com.mesh_suite.controller.notify;

import com.mesh_suite.service.notify.S3Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/mesh-suite/v1.0/s3/resource")
@Tag(name = "S3 Bucket Resource Service")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping(value = "/file/{userId}/{companyId}/{formId}/{file}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file,
                                             @PathVariable Long userId,
                                             @PathVariable Long companyId,
                                             @PathVariable Long formId) {
        return ResponseEntity.ok(s3Service.uploadFile(file, userId, companyId, formId));
    }

    @PostMapping(value = "/issued/{userId}/{companyId}/{formId}/{file}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadDocument(@RequestPart("file") MultipartFile file,
                                                 @PathVariable Long userId,
                                                 @PathVariable Long companyId,
                                                 @PathVariable Long formId) {
        return ResponseEntity.ok(s3Service.uploadDocument(file, userId, companyId, formId));
    }

    @GetMapping("/user-files/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUploadedFilesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(s3Service.getUploadedFilesByUserId(userId));
    }

    @GetMapping("/company/issued-docs/{companyId}")
    public ResponseEntity<List<Map<String, Object>>> getUploadedFilesByCompanyId(@PathVariable Long companyId) {
        return ResponseEntity.ok(s3Service.getUploadedDocsByCompanyId(companyId));
    }

    @GetMapping("/user-files/{userId}/{formId}")
    public ResponseEntity<List<Map<String, Object>>> getUploadedFilesByUserIdAndFormId(
            @PathVariable Long userId,
            @PathVariable Long formId) {
        return ResponseEntity.ok(s3Service.getUploadedFilesByUserIdAndFormId(userId, formId));
    }

    @GetMapping("/all-issued-docs/{userId}/{companyId}")
    public ResponseEntity<List<Map<String, Object>>> getUploadedDocsByUserIdAndCompanyId(
            @PathVariable Long userId,
            @PathVariable Long companyId) {
        return ResponseEntity.ok(s3Service.getUploadedDocsByUserIdAndCompanyId(userId, companyId));
    }

    @GetMapping("/issued-docs/{formId}/{companyId}/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getIssuedDocsByCompanyIdAndFormId(
            @PathVariable Long companyId,
            @PathVariable Long formId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                s3Service.getIssuedDocsByCompanyIdAndFormIdAndUserId(companyId, formId, userId)
        );
    }

    @PostMapping(value = "/upload/{file}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(s3Service.uploadFile(file));
    }
}