package com.mesh_suite.domain.company;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "module")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "module_name")
    private String moduleName;
    @Column(name = "module_description")
    private String moduleDescription;

    @Column(name = "admin_features")
    private String adminFeatures;

    @Column(name = "client_features")
    private String clientFeatures;
}
