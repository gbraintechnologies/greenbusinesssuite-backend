package com.mesh_suite.domain.company;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "company_branding")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Company Branding table", name = "Company Branding")
public class CompanyBranding implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenancy_id", nullable = false)
    private String tenancyId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "company_name")
    private String companyName;

    private String logo;

    private String color;

    @ElementCollection
    @CollectionTable(name = "company_module_ids", joinColumns = @JoinColumn(name = "branding_id"))
    @Column(name = "module_id")
    private Set<Long> moduleIds;

    @ElementCollection
    @CollectionTable(name = "company_category_specific_module_ids", joinColumns = @JoinColumn(name = "branding_id"))
    @Column(name = "category_specific_module_id")
    private Set<Long> categorySpecificModuleIds;
}
