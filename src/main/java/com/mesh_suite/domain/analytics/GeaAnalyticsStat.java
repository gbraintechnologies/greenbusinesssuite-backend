package com.mesh_suite.domain.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "gea_analytics_stat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeaAnalyticsStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String scope;

    @Column(name = "program_id", nullable = false, length = 64)
    private String programId;

    @Column(nullable = false, length = 32)
    private String dimension;

    @Column(nullable = false, length = 512)
    private String name;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal value;

    @Column(name = "refreshed_at", nullable = false)
    private Instant refreshedAt;
}
