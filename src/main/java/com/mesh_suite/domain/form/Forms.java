
package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mesh_suite.util.FormUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "forms")
@Data
@AllArgsConstructor
@Schema(description = "Forms table", name = "Form Schema")
public class Forms implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "url")
    private String url;

    private String description;

    @Column(name = "form_instruction")
    private String formInstruction;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("ordering ASC")  // order by `ordering` field
    private List<FormSections> formSections = new ArrayList<>();

    @Column(name = "user_mandatory", columnDefinition = "boolean default false")
    private Boolean userMandatory;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormUtils.PublishStatus publishStatus = FormUtils.PublishStatus.DRAFT;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    @Schema(defaultValue = "false")
    private Boolean isDeleted = false;

    @Column(name = "is_template", nullable = false, columnDefinition = "boolean default false")
    @Schema(defaultValue = "false")
    private Boolean isTemplate= false;

    @Schema(defaultValue = "GENERAL")
    private String layout = "GENERAL";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "deleted_on")
    private LocalDateTime deletedOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "assign_date")
    private LocalDateTime assignDate;


    @Column(name = "is_anonymous", columnDefinition = "boolean default false")
    @Schema(defaultValue = "false")
    private Boolean isAnonymous = false;

    @Column(name = "multiple_forms", nullable = false, columnDefinition = "boolean default false")
    @Schema(defaultValue = "false")
    private Boolean multipleForms = false;

    @Column(name = "redirect_url")
    private String redirectUrl;
    public Forms() {
        this.formSections = new ArrayList<>();
    }
    public Forms(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Forms)) return false;
        Forms forms = (Forms) o;
        return Objects.equals(id, forms.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); 
    }
}
