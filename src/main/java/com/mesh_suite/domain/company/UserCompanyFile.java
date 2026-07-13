package com.mesh_suite.domain.company;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_company_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCompanyFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
}
