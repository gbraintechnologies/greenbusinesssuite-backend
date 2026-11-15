package com.mesh_suite.domain.suite;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "suite_apps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuiteApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 255)
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 512)
    private String href;

    @Column(length = 120)
    private String category;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    @Column(length = 8)
    private String mark;

    @Column(length = 64)
    private String accent;

    @Column(length = 64)
    private String wash;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "updated_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime updatedOn;
}
