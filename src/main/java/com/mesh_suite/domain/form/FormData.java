package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mesh_suite.constant.forms.FormResponseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "forms_response_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"inputData"})
public class FormData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id")
    private Long formId;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FormResponseStatus status = FormResponseStatus.PENDING;

    @OneToOne(mappedBy = "formData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private InputData inputData;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "Date form response data was created")
    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "Last Date when the form response data was updated")
    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}