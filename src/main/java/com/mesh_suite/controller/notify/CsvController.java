package com.mesh_suite.controller.notify;

import com.mesh_suite.service.notify.CsvService;
import com.opencsv.exceptions.CsvValidationException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/mesh-suite/v1.0/csv")
@Tag(name = "CSV File Upload")
@Slf4j
public class CsvController {
    @Autowired
    private CsvService csvService;


    @PostMapping(value = "/sector/upload/{file}",consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadCsv(@RequestPart("file") MultipartFile file) {
        try {
            csvService.uploadCsv(file);
            return ResponseEntity.ok("File uploaded and processed successfully.");
        } catch (IOException e) {
            log.error("IOException while uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: IOException occurred.");
        } catch (CsvValidationException e) {
            log.error("CsvValidationException while uploading file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to upload file: CSV validation error.");
        } catch (Exception e) {
            log.error("Exception while uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: An unexpected error occurred.");
        }
    }
}
