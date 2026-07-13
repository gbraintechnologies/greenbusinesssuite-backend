package com.mesh_suite.producer;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.dao.notify.NotificationMessageRepository;
import com.mesh_suite.domain.notify.NotificationMessage;
import com.mesh_suite.dto.NotificationMessageDTO;
import com.mesh_suite.service.notify.NotificationMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class NotificationProcessor {

    private final NotificationMessageRepository notifyMsgRepo;
    private final NotificationMessageService notificationMessageService;

    public NotificationProcessor(NotificationMessageRepository notifyMsgRepo,
                                 NotificationMessageService notificationMessageService) {
        this.notifyMsgRepo = notifyMsgRepo;
        this.notificationMessageService = notificationMessageService;
    }

    @Transactional
    public void processNotificationsForTenant(String tenantId) {

        log.info("Processing notifications for tenant: {}", tenantId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeWindow = now.plusMinutes(10);
        List<CompanyStatus.RecurringType> recurringTypes = Arrays.asList(
                CompanyStatus.RecurringType.DAILY, CompanyStatus.RecurringType.WEEKLY, CompanyStatus.RecurringType.BI_WEEKLY,
                CompanyStatus.RecurringType.MONTHLY, CompanyStatus.RecurringType.QUARTERLY, CompanyStatus.RecurringType.ANNUAL
        );

        log.info("Retrieving scheduled messages between {} and {}", now, now.plusMinutes(10));

        List<NotificationMessage> readyMessages = notifyMsgRepo.findValidNotifications(
                recurringTypes, now, timeWindow
        );
        readyMessages.forEach(msg -> log.info("Fetched Message: {}, Trigger Time: {}", msg.getId(), msg.getTriggerTime()));

        if (!readyMessages.isEmpty()) {
            //process the notifications
            readyMessages.forEach(this::sendNotification);
            // Update next trigger time for processed notifications
            batchUpdateNextTriggerTime(readyMessages);
        } else {
            log.info("No notifications ready for processing in this time window.");
        }
    }

    private void sendNotification(NotificationMessage message) {
        try {
            switch (message.getMessageType()) {
                case EMAIL -> notificationMessageService.sendEmail(new NotificationMessageDTO(message));
                case SMS -> notificationMessageService.sendSms(new NotificationMessageDTO(message));
                default -> log.error("Unsupported message type for message ID: {}", message.getId());
            }
            log.info("Notification sent for message ID: {}", message.getId());
        } catch (Exception e) {
            log.error("Failed to send notification for message ID: {}", message.getId(), e);
        }
    }

    private void batchUpdateNextTriggerTime(List<NotificationMessage> messages) {
        messages.forEach(this::updateNextTriggerTime);
        notifyMsgRepo.saveAll(messages);
    }

    private void updateNextTriggerTime(NotificationMessage message) {
        if (message == null || message.getTriggerTime() == null || message.getRecurringType() == null) {
            log.warn("Invalid notification message or missing data, skipping update.");
            return;
        }

        LocalDateTime nextTriggerTime = message.getTriggerTime();
        CompanyStatus.RecurringType recurringType = message.getRecurringType();
        LocalDateTime endDate = message.getEndDate();

        // Validate if the initial trigger time is already beyond the end date
        if (endDate != null && nextTriggerTime.isAfter(endDate)) {
            log.info("Initial trigger time for message ID: {} exceeds the end date. Skipping.", message.getId());
            return;
        }

        // Increment to the next valid trigger time based on the original trigger time
        switch (recurringType) {
            case DAILY -> nextTriggerTime = nextTriggerTime.plusDays(1);
            case WEEKLY -> nextTriggerTime = nextTriggerTime.plusWeeks(1);
            case BI_WEEKLY -> nextTriggerTime = nextTriggerTime.plusWeeks(2);
            case MONTHLY -> nextTriggerTime = nextTriggerTime.plusMonths(1);
            case QUARTERLY -> nextTriggerTime = nextTriggerTime.plusMonths(3);
            case ANNUAL -> nextTriggerTime = nextTriggerTime.plusYears(1);
            default -> {
                log.warn("Unsupported recurring type for message ID: {}", message.getId());
                return;
            }
        }

        // Ensure the new trigger time does not exceed the end date
        if (endDate != null && nextTriggerTime.isAfter(endDate)) {
            log.info("Next trigger time for message ID: {} exceeds the end date. Skipping.", message.getId());
            return;
        }

        // Update the trigger time and increment the times sent
        message.setTriggerTime(nextTriggerTime);
        message.setTimesSent(message.getTimesSent() + 1);

        log.info("Next trigger time for message ID: {} updated to {}", message.getId(), nextTriggerTime);
    }
}
