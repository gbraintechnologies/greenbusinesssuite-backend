package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "forms_section_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"inputData"})
@ToString(exclude = {"formDataFields"})
public class FormDataSection implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "form_section_id")
    private Long formSectionId;
    @Column(name = "form_section_name")
    private String formSectionName;

    @ManyToOne
    @JoinColumn(name = "input_data_id", nullable = false)
    @JsonBackReference
    private InputData inputData;

    @OneToMany(mappedBy = "formSection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FormDataField> formDataFields;
}