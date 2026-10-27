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

@Entity
@Table(name = "gea_program_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeaProgramRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "program_id", nullable = false, length = 64)
    private String programId;

    @Column(name = "record_type", nullable = false, length = 32)
    private String recordType;

    @Column(name = "external_key", length = 256)
    private String externalKey;

    @Column(length = 32)
    private String gender;

    @Column(length = 64)
    private String region;

    @Column(length = 512)
    private String sector;

    private Boolean registered;

    private Boolean disability;

    @Column(name = "age_band", length = 32)
    private String ageBand;

    @Column(length = 64)
    private String literacy;

    @Column(name = "application_type", length = 64)
    private String applicationType;

    @Column(name = "application_status", length = 128)
    private String applicationStatus;

    @Column(name = "requested_amount", precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "approved_amount", precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "training_type", length = 256)
    private String trainingType;
}
