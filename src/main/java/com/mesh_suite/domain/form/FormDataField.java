package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
@Entity
@Table(name = "forms_field_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"formSection"})
@ToString(exclude = {"formSection"})
public class FormDataField implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_field_id")
    private Long formFieldId;

    @Column(name = "field_name")
    private String fieldName;

    private String response;

    @Column(name = "is_statistical_field")
    private Boolean isStatisticalField = false;

    @Column(name = "statistical_function")
    private String statisticalFunction;

    @Column(name = "display_type")
    private String displayType;

    @ManyToOne
    @JoinColumn(name = "form_section_id", nullable = false)
    @JsonBackReference
    private FormDataSection formSection;

    public boolean isStatisticalField() {
        return isStatisticalField != null && isStatisticalField;
    }
}