package com.mesh_suite.domain.company;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_specific_module")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"category"})
public class CategorySpecificModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "admin_features")
    private String adminFeatures;

    @Column(name = "client_features")
    private String clientFeatures;

    @Column(name = "is_template", nullable = false)
    private boolean isTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @JsonBackReference
    private CategorySetup category;
}

