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
@Table(name = "forms_input_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"formData"})
@ToString(exclude = {"formSections"})
public class InputData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "form_data_id", nullable = false)
    @JsonBackReference
    private FormData formData;

    @OneToMany(mappedBy = "inputData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FormDataSection> formSections;
}
