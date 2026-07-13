package com.mesh_suite.service.notify;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.constant.notify.MessageType;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.dao.form.FormDataRepository;
import com.mesh_suite.dao.form.FormsRepository;
import com.mesh_suite.dao.notify.NotificationMessageRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.domain.form.Forms;
import com.mesh_suite.domain.notify.NotificationMessage;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.DocIssueDetails;
import com.mesh_suite.dto.EmailRequestDetails;
import com.mesh_suite.dto.NotificationMessageDTO;
import com.mesh_suite.dto.NotificationResponseDto;
import com.mesh_suite.exception.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NotificationMessageService {

    @Autowired
    private NotificationMessageRepository notifyMsgRepo;
    @Autowired
    private FormsRepository formsRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private S3Service s3Service;

    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FormDataRepository formDataRepository;
    public NotificationMessage getMessageById(Long id) {
        return notifyMsgRepo.findById(id)
                .orElseThrow(() -> new NotificationException("Notification message not found with id: " + id));
    }

    @Transactional
    public Long sendSms(NotificationMessageDTO sms) {
        log.info("Processing SMS notification for sender: {}", sms.getSender());

        // Retrieve existing NotificationMessage or create a new one
        NotificationMessage smsReq = Optional.ofNullable(sms.getId())
                .flatMap(notifyMsgRepo::findById)
                .orElseGet(() -> {
                    NotificationMessage newMessage = new NotificationMessage(sms);
                    newMessage.setMessageType(MessageType.SMS);
                    newMessage.setTimesSent(0); // Initialize timesSent for new notification
                    LocalDateTime dateTime = setNextTriggerTime(newMessage.getTriggerTime(), newMessage.getRecurringType());
                    newMessage.setTriggerTime(dateTime);
                    log.debug("Created a new notification for sender: {}", sms.getSender());
                    return newMessage;
                });

        // Increment the timesSent field
        int timesSent = Optional.ofNullable(smsReq.getTimesSent()).orElse(0) + 1;
        smsReq.setTimesSent(timesSent);
        log.debug("Updated timesSent for notification ID: {} to {}", smsReq.getId(), timesSent);

        // total recipients
        int totalRecipients = Optional.ofNullable(smsReq.getRecipients()).map(List::size).orElse(0);
        smsReq.setTotalRecipients(totalRecipients);
        log.debug("Total recipients for SMS ID: {} set to: {}", smsReq.getId(), totalRecipients);

        // Save the notification (new or updated)
        Long smsId = notifyMsgRepo.save(smsReq).getId();
        log.info("SMS notification saved with ID: {}", smsId);

       /* // Send SMS to all recipients
        smsReq.getRecipients().forEach(recipient -> {
            log.debug("Sending SMS to recipient: {}", recipient);
            SmsDTO smsDTO = SmsDTO.builder()
                    .toNumber(recipient)
                    .from(smsReq.getSender())
                    .message(smsReq.getBody())
                    .build();

            notificationProducer.pushSMSToQueue(smsDTO);
            log.info("SMS pushed to queue for recipient: {}", recipient);
        });

        log.info("SMS processing completed for ID: {}", smsId);*/
        return smsId;
    }

    @Transactional
    public Long sendEmail(NotificationMessageDTO email) {
        log.info("Processing email notification for sender: {}", email.getSender());

        // Retrieve existing NotificationMessage or create a new one
        NotificationMessage emailReq = Optional.ofNullable(email.getId())
                .flatMap(notifyMsgRepo::findById)
                .orElseGet(() -> {
                    NotificationMessage newMessage = new NotificationMessage(email);
                    newMessage.setMessageType(MessageType.EMAIL);
                    newMessage.setTimesSent(0); // Initialize timesSent for new notification
                    LocalDateTime dateTime = setNextTriggerTime(newMessage.getTriggerTime(), newMessage.getRecurringType());
                    newMessage.setTriggerTime(dateTime);
                    return newMessage;
                });

        // Increment the timesSent field
        int timesSent = Optional.ofNullable(emailReq.getTimesSent()).orElse(0) + 1;
        emailReq.setTimesSent(timesSent);

        int totalRecipients = Optional.ofNullable(emailReq.getRecipients()).map(List::size).orElse(0);
        emailReq.setTotalRecipients(totalRecipients);
        log.debug("Total recipients for EMAIL ID: {} set to: {}", emailReq.getId(), totalRecipients);

        Long messageId = notifyMsgRepo.save(emailReq).getId();
        log.info("Notification saved with ID: {}", messageId);

        // Send emails to all recipients
        emailReq.getRecipients().forEach(recipient -> {
            emailService.sendEmail(String.valueOf(email.getRecipients()),email.getSubject(),email.getBody(),email.getIsHtml(),email.getFileName());
        });

        log.info("Email processing completed for notification ID: {}", messageId);
        return messageId;
    }
    @Transactional // with single file attachment
    public Long sendEmail(NotificationMessageDTO email, MultipartFile attachment) {
        if (email.getRecipients() == null || email.getRecipients().isEmpty()) {
            throw new IllegalArgumentException("Recipients list cannot be null or empty");
        }

        // Create a new NotificationMessage from the DTO
        NotificationMessage emailReq = new NotificationMessage(email);
        emailReq.setMessageType(MessageType.EMAIL);


        // Initialize timesSent to 0 if null and log the current value
        Integer timesSent = emailReq.getTimesSent() != null ? emailReq.getTimesSent() : 0;
        log.debug("Current timesSent for email: {} | Setting to: {}", emailReq.getSubject(), timesSent + 1);
        emailReq.setTimesSent(timesSent + 1);

        // Save the email notification to the database
        Long messageId = notifyMsgRepo.save(emailReq).getId();
        log.info("Email notification saved with ID: {} for sender: {}", messageId, emailReq.getSender());

        // Iterate through recipients and send the email
        for (String recipient : emailReq.getRecipients()) {
            log.debug("Sending email to recipient: {}", recipient);

            emailService.sendEmail(
                    String.valueOf(email.getRecipients()),
                    email.getSubject(),
                    email.getBody(),
                    email.getIsHtml(),
                    email.getFileName());
        }
        log.info("Email processing completed for ID: {}", messageId);
        return messageId;
    }

    public NotificationResponseDto getRecurringTypeMessages(CompanyStatus.RecurringType type, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<NotificationMessage> msgPaginated = notifyMsgRepo.findByRecurringType(type, pageable);
        return notificationMessagePaginate(msgPaginated);
    }
    public NotificationResponseDto getAllMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<NotificationMessage> msgPaginated = notifyMsgRepo.findAll( pageable);
        return notificationMessagePaginate(msgPaginated);
    }

    public NotificationResponseDto notificationMessagePaginate(Page<NotificationMessage> notificationMessages) {
        NotificationResponseDto notificationResponseDto = new NotificationResponseDto();
        notificationResponseDto.setFirst(notificationMessages.isFirst());
        notificationResponseDto.setLast(notificationMessages.isLast());
        notificationResponseDto.setTotalElements(notificationMessages.getTotalElements());
        notificationResponseDto.setTotalPages(notificationMessages.getTotalPages());
        notificationResponseDto.setSize(notificationMessages.getSize());
        notificationResponseDto.setContent(notificationMessages.getContent());
        return notificationResponseDto;
    }
    public NotificationMessage updateRecurringType(Long id, CompanyStatus.RecurringType recurringType) {
        NotificationMessage notificationMessage = notifyMsgRepo.findById(id)
                .orElseThrow(() -> new NotificationException("NotificationMessage not found with id " + id));

        notificationMessage.setRecurringType(recurringType);
        return notifyMsgRepo.save(notificationMessage);
    }
    @Transactional
    public void deleteById(Long id) {
        notifyMsgRepo.deleteById(id);
    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        ids.forEach(notifyMsgRepo::deleteById);
    }
    public NotificationResponseDto getNonRecurringNotifications(Pageable pageable) {
        Page<NotificationMessage> notificationMessages = notifyMsgRepo.findByRecurringTypeNot(CompanyStatus.RecurringType.NON_RECURRING, pageable);
        return notificationMessagePaginate(notificationMessages);
    }

    @Async
    public void sendFormAssignmentEmail(String companyAdminFirstName, String formTitle, String assignDate, String recipientEmail) {
        try {
            emailService.sendFormAssignmentEmail(
                    companyAdminFirstName,
                    formTitle,
                    assignDate,
                    recipientEmail);
            log.info("Form assignment email sent successfully to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send form assignment email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    /**
     * Sends an email to the company admin when a form is submitted.
     */
    @Async
    public void sendFormSubmissionEmail(EmailRequestDetails emailReq) {
        try {

            if (emailReq.getCompanyAdminEmail() == null || emailReq.getCompanyAdminEmail().isBlank()) {
                throw new IllegalArgumentException("Company admin email is required.");
            }

            String submissionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            log.info("Preparing to send form submission email: form='{}', recipient='{}'",
                    emailReq.getUserFullName(), emailReq.getCompanyAdminEmail());

            emailService.sendFormSubmissionEmail(
                    emailReq.getCompanyAdminFirstName(),
                    emailReq.getUserFullName(),
                    emailReq.getUserFullName(),
                    submissionDate,
                    emailReq.getCompanyAdminEmail()
            );

            log.info("Form submission email for '{}' sent to '{}'", emailReq.getUserFullName(), emailReq.getCompanyAdminEmail());

        } catch (IllegalArgumentException ex) {
            log.warn("Validation error while sending form submission email: {}", ex.getMessage());
            throw ex;
        } catch (Exception e) {
            log.error("Unexpected error while sending form submission email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send form submission email", e);
        }
    }

    /**
     * Sends an email to a user when their form response is completed.
     */
    @Async
    public void sendFormResponseEmail(Long formId, Long userId, Boolean isCompleted) {
        try {
            Forms form = formsRepository.findById(formId)
                    .orElseThrow(() -> new IllegalArgumentException("Form not found for ID: " + formId));

            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));

            if (!Boolean.TRUE.equals(isCompleted)) {
                log.info("Form '{}' is not marked completed — skipping email.", form.getName());
                return;
            }

            emailService.sendFormResponseEmail(
                    form.getName(),
                    form.getUrl(),
                    user.getEmail());

            log.info("Form response email for '{}' sent to '{}'", form.getName(), user.getEmail());

        } catch (Exception e) {
            log.error("Error sending form response email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send form response email", e);
        }
    }


    // Send Document Issuance Email
    public void sendDocumentIssuanceEmail(DocIssueDetails docIssueDetails) {
        try {
            Users user = userRepository.findById(docIssueDetails.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + docIssueDetails.getUserId()));

            UserCompany companyDetails = userCompanyRepository.findById(docIssueDetails.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with ID " + docIssueDetails.getCompanyId()));

            emailService.sendDocumentIssuanceEmail(
                    companyDetails.getCompanyName(),
                    docIssueDetails.getDocLink(),
                    user.getEmail());

            log.info("Document issuance email sent to {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send document issuance email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send document issuance email", e);
        }
    }


    public  LocalDateTime setNextTriggerTime(LocalDateTime currentTriggerTime, CompanyStatus.RecurringType recurringType) {
        return switch (recurringType) {
            case DAILY -> currentTriggerTime.plusDays(1);
            case WEEKLY -> currentTriggerTime.plusWeeks(1);
            case BI_WEEKLY -> currentTriggerTime.plusWeeks(2);
            case MONTHLY -> currentTriggerTime.plusMonths(1);
            case QUARTERLY -> currentTriggerTime.plusMonths(3);
            case ANNUAL -> currentTriggerTime.plusYears(1);
            case NON_RECURRING -> currentTriggerTime;
            default -> {
                log.error("Unsupported recurring type");
                yield currentTriggerTime;
            }
        };
    }

}
