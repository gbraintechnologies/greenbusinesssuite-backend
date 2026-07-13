
package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "form_field")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data collection form field table", name = "Form Field Schema")
@JsonIgnoreProperties({"formSection"})
public class FormField implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String label;

    @Column(name = "place_holder")
    private String placeHolder;

    @ManyToOne
    @JoinColumn(name = "form_section_id")
    @JsonBackReference
    private FormSections formSection;

    private String instruction;

    private Integer ordering;
    @Column(name = "max_length")
    private Long maxLength;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Schema(defaultValue = "false")
    private Boolean isDeleted = false;

    @Column(name = "field_data_type")
    private String fieldDataType;

    @ElementCollection
    @CollectionTable(name = "form_field_choice_values", joinColumns = @JoinColumn(name = "form_field_id"))
    @Column(name = "choice_value")
    private List<String> choiceValue = new ArrayList<>();

    @Schema(defaultValue = "false")
    private Boolean isMandatory=false;

    @Schema(defaultValue = "false")
    private Boolean horizontalAlign =false;

    private String validPattern;
    @Schema(defaultValue = "false")
    private Boolean isStatisticalField =false;
    private String statisticalFunction;
    private String displayType;

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
}
