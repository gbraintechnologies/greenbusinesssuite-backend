package com.mesh_suite.domain.notify;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.constant.notify.MessageType;
import com.mesh_suite.dto.NotificationMessageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification_message")
public class NotificationMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    @ElementCollection
    @CollectionTable(name = "notification_recipients", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "recipient")
    private List<String> recipients = new ArrayList<>();

    @Column(name = "total_recipients")
    private Integer totalRecipients;

    private String subject;
    @Column(name = "file_name")
    private String fileName;
    
    @Column(columnDefinition = "TEXT")
    private String body;

    @Schema(defaultValue = "false")
    @Column(name = "is_html")
    private Boolean isHtml;


    @Enumerated(EnumType.STRING)
    @Column(name = "recurring_type")
    private CompanyStatus.RecurringType recurringType;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Column(name = "trigger_time")
    private LocalDateTime triggerTime;
    @Column(name = "times_sent")
    private Integer timesSent;
    @Schema(description = "Date Email was created")
    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    public NotificationMessage(NotificationMessageDTO dto) {
        this.sender = dto.getSender();
        this.recipients = dto.getRecipients();
        this.recipients = new ArrayList<>(dto.getRecipients());
        this.subject = dto.getSubject();
        this.fileName = dto.getFileName();
        this.body = dto.getBody();
        this.isHtml = dto.getIsHtml();
        this.recurringType = dto.getRecurringType();
        this.triggerTime = dto.getTriggerTime();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
    }
}
