
package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "form_sections")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data collection form section table", name="Form Section Schema")
@JsonIgnoreProperties({"form"})
public class FormSections implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Form Section name")
    private String name;

    @Schema(description = "Description of form section")
    private String description;

    @Schema(description = "Form section instruction")
    private String instruction;

    @ManyToOne
    @JoinColumn(name = "form_id")
    @JsonBackReference
    private Forms form;

    @OneToMany(mappedBy = "formSection", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("ordering ASC")  // Ensure ordering by `ordering` field
    private Set<FormField> formFields = new HashSet<>();
    private Integer ordering;
    @Column(nullable = false, columnDefinition = "boolean default false")
    @Schema(defaultValue = "false")
    private Boolean isDeleted = false;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormSections)) return false;
        FormSections that = (FormSections) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
