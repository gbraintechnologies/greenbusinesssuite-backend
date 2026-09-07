package com.mesh_suite.domain.notify;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mesh_suite.constant.notify.MediaType;
import com.mesh_suite.interceptor.TenantContext;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_center")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class MediaCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    @NotNull(message = "Media type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;
    @Column(name = "thumbnail")
    private String thumbnail;
    @Column(name = "alt_text")
    private String altText;
    @Column(name = "heading")
    private String heading;
    @Column(name = "url")
    private String url;
    @Column(name = "is_active", columnDefinition = "boolean default false")
    private Boolean isActive = false;

    @CreationTimestamp
    @Column(name = "created_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdOn;
    @UpdateTimestamp
    @Column(name = "updated_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime updatedOn;

    @PrePersist
    private void assignTenant() {
        if (tenantId == null) {
            tenantId = TenantContext.getCurrentTenant();
        }
    }
}