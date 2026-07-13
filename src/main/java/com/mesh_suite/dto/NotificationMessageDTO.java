package com.mesh_suite.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.domain.notify.NotificationMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDTO {
    private Long id;
    private String sender;
    private List<String> recipients = new ArrayList<>();
    private String subject;
    private String fileName;
    private String body;
    private Boolean isHtml;
    private CompanyStatus.RecurringType recurringType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime triggerTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime endDate;

    public NotificationMessageDTO(NotificationMessage message) {
        this.id = message.getId();
        this.sender = message.getSender();
        this.recipients = message.getRecipients();
        this.subject = message.getSubject();
        this.body = message.getBody();
        this.isHtml = message.getIsHtml();
        this.recurringType = message.getRecurringType();
        this.triggerTime = message.getTriggerTime();
        this.startDate = message.getStartDate();
        this.endDate = message.getEndDate();
    }
}
