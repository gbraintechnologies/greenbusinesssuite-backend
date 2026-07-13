package com.mesh_suite.domain.form;

import com.mesh_suite.constant.forms.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discount_data")
public class DiscountedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "discount_id")
    private Long discountId;
    @Column(name = "service_name")
    private String serviceName;
    @Column(name = "original_amount")
    private BigDecimal originalAmount;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    @Column(name = "discounted_price")
    private BigDecimal discountedPrice;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted = false;
    @CreationTimestamp
    private LocalDateTime createdOn;

}
