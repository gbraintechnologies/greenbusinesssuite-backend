package com.mesh_suite.controller.notify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.domain.notify.NotificationMessage;
import com.mesh_suite.dto.DocIssueDetails;
import com.mesh_suite.dto.EmailRequestDetails;
import com.mesh_suite.dto.NotificationMessageDTO;
import com.mesh_suite.dto.NotificationResponseDto;
import com.mesh_suite.service.notify.NotificationMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/mesh-suite/v1.0/notifications")
@Tag(name = "Notification Center API", description = "Notifications Related Operations")
@Slf4j
public class NotificationMessageController {
    @Autowired
    public NotificationMessageService notificationMessageService;

  @Operation(summary = "Send an Email notification")
    @PostMapping(value = "/email/push")
    public ResponseEntity<Long> sendEmail(
            @Parameter(description = "Email details") @RequestBody NotificationMessageDTO email) {
        Long emailId = notificationMessageService.sendEmail(email);
        return ResponseEntity.ok(emailId);
    }

  @Operation(summary = "Send an Email notification with optional attachment")
    @PostMapping(value = "/attach-email/push", consumes = {"multipart/form-data"})
    public ResponseEntity<Long> sendEmailWithAttachment(
            @Parameter(description = "Email details in JSON format") @RequestPart("email") String emailJson,
            @Parameter(description = "Attachment file") @RequestPart(value = "attachment", required = false) MultipartFile attachment) throws JsonProcessingException {


       ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        NotificationMessageDTO email = objectMapper.readValue(emailJson, NotificationMessageDTO.class);

        Long emailId = notificationMessageService.sendEmail(email, attachment);
        return ResponseEntity.ok(emailId);
    }

    @Operation(summary = "Send an SMS notification")
    @PostMapping("/sms/push")
    public ResponseEntity<Long> sendSms(@RequestBody NotificationMessageDTO sms) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(notificationMessageService.sendSms(sms));
    }
    @Operation(summary = "Get recurring type notification messages")
    @GetMapping("/messages-by-type/{page}/{size}/{type}")
    public ResponseEntity<NotificationResponseDto> getRecurringTypeMessages(
            @PathVariable int page,
            @PathVariable int size,
            @PathVariable CompanyStatus.RecurringType type) {
        NotificationResponseDto notificationResponseDto = notificationMessageService.getRecurringTypeMessages(type, page, size);
        return new ResponseEntity<>(notificationResponseDto, HttpStatus.OK);
    }

    @Operation(summary = "Get all notification messages")
    @GetMapping("/all-messages/{page}/{size}")
    public ResponseEntity<NotificationResponseDto> getAllMessages(
            @PathVariable int page,
            @PathVariable int size) {
        NotificationResponseDto notificationResponseDto = notificationMessageService.getAllMessages(page, size);
        return new ResponseEntity<>(notificationResponseDto, HttpStatus.OK);
    }
    @Operation(summary = "Update the recurring type of an existing notification message by ID")
    @PutMapping("/notify-change/{id}/{recurringType}")
    public ResponseEntity<NotificationMessage> updateRecurringType(
            @PathVariable Long id, @PathVariable CompanyStatus.RecurringType recurringType) {
        NotificationMessage updatedMessage = notificationMessageService.updateRecurringType(id, recurringType);
        return ResponseEntity.ok(updatedMessage);
    }
    @Operation(summary = "Retrieve a notification message by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationMessage> getNotificationMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationMessageService.getMessageById(id));
    }

    @Operation(summary = "Delete a notification message by its ID")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteNotificationById(@PathVariable Long id) {
        notificationMessageService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Notification message with ID " + id + " deleted successfully.");
    }

    @Operation(summary = "Delete multiple notification messages by their IDs")
    @DeleteMapping("/delete/batch")
    public ResponseEntity<String> deleteNotificationsByIds(@RequestBody List<Long> ids) {
        notificationMessageService.deleteByIds(ids);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Notification messages with IDs " + ids + " deleted successfully.");
    }
    @Operation(summary = "Get all recurring type notification messages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of paginated notification messages"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/recurring-messages/{page}/{size}")
    public NotificationResponseDto getNonRecurringNotifications(
            @PathVariable int page,
            @PathVariable int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        return notificationMessageService.getNonRecurringNotifications(pageable);
    }

    @PostMapping("/form-submit-email")
    @Operation(
            summary = "Send form submission email",
            description = "Sends an email notification to the company admin when a form is submitted."
    )
    public ResponseEntity<String> sendFormSubmissionEmail(@RequestBody EmailRequestDetails emailReq) {
        try {
            notificationMessageService.sendFormSubmissionEmail(emailReq);
            return ResponseEntity.ok("Form submission email sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send form submission email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send form submission email.");
        }
    }


    @PostMapping("/form-response-email/{userId}/{formId}/{isCompleted}")
    @Operation(summary = "Send form response email",
            description = "Send an email notification to the user when a form response is received.")
    public ResponseEntity<String> sendFormResponseEmail(@PathVariable Long formId,
                                                        @PathVariable Long userId,
                                                        @PathVariable Boolean isCompleted) {
            notificationMessageService.sendFormResponseEmail(formId, userId,isCompleted);
            return ResponseEntity.ok("Form response email sent successfully.");
    }

    @Operation(summary = "Send Document Issuance Link send to user Email")
    @PostMapping("/send-document-issuance")
    public ResponseEntity<String> sendDocumentIssuanceEmail(@RequestBody DocIssueDetails docIssueDetails) {

        notificationMessageService.sendDocumentIssuanceEmail(docIssueDetails);
        return ResponseEntity.ok("Document issuance email sent successfully.");
    }

}

