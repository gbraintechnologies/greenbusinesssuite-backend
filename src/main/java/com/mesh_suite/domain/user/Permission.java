package com.mesh_suite.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;
    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 50)
    private String module;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "sub_module", length = 50)
    private String subModule;

    // Helper to create permission names
    public static String buildName(String module, String subModule, String action) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(module.toLowerCase().replace(" ", "_"));
        if (subModule != null && !subModule.isBlank()) {
            nameBuilder.append(".").append(subModule.toLowerCase().replace(" ", "_"));
        }
        return nameBuilder.append(":").append(action.toLowerCase()).toString();
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (!name.equals(buildName(module, subModule, action))) {
            throw new IllegalStateException("Permission name doesn't match module/sub_module/action");
        }
    }

    public String getName() {
        return name;
    }
}
