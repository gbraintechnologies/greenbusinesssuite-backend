package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mesh_suite.interceptor.TenantContext;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Filter;

import java.io.Serializable;
import java.util.List;
@Entity
@Table(name = "forms_input_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"formData"})
@ToString(exclude = {"formSections"})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class InputData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @OneToOne
    @JoinColumn(name = "form_data_id", nullable = false)
    @JsonBackReference
    private FormData formData;

    @OneToMany(mappedBy = "inputData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FormDataSection> formSections;

    @PrePersist
    private void assignTenant() {
        if (tenantId == null) {
            tenantId = TenantContext.getCurrentTenant();
        }
    }
}
