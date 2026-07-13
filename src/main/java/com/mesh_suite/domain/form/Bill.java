package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mesh_suite.constant.forms.BillingType;
import com.mesh_suite.constant.forms.Frequency;
import com.mesh_suite.constant.forms.PaymentMethod;
import com.mesh_suite.constant.forms.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "billing")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id")
    private Long formId;
    @Column(name = "discount_id", nullable = true)
    private Long discountId;
    @Column(name = "service_name")
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type")
    private BillingType billingType;

    @Column(name = "currency")
    private String currency;

    @Column(name = "amount")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = true)
    private Frequency frequency;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @CreationTimestamp
    @Column(name = "created_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "updated_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime updatedOn;
}
