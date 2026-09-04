package com.mesh_suite.domain.company;

import com.mesh_suite.interceptor.TenantContext;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_company_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class UserCompanyFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "form_id")
    private Long formId;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "user_id")
    private Long userId;

    @PrePersist
    private void assignTenant() {
        if (tenantId == null) {
            tenantId = TenantContext.getCurrentTenant();
        }
    }
}
