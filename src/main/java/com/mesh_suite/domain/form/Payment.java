package com.mesh_suite.domain.form;

import com.mesh_suite.constant.forms.Network;
import com.mesh_suite.constant.forms.PaymentMethod;
import com.mesh_suite.constant.forms.PaymentStatus;
import com.mesh_suite.dto.PaymentDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "bill_id", nullable = false)
    private Long billId;

    @Column(name = "response_id", unique = true)
    private Long responseId;

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;   // GATEWAY ID (trans_id)

    @Column(name = "trans_ref")
    private String transRef; // MY ID for ref (exttrid)

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "bank_code")
    private String bankCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "network")
    private Network network;

    @Column(name = "date_paid")
    private LocalDateTime datePaid;
    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    // Constructor that accepts PaymentDto
    public Payment(PaymentDto paymentDto) {
        this.billId = paymentDto.getBillId();
        this.responseId = paymentDto.getResponseId();
        this.paymentMethod = paymentDto.getPaymentMethod();
        this.status = PaymentStatus.PENDING; // always start as PENDING, never trust client
        this.transRef = paymentDto.getTransRef();
        this.serviceName = paymentDto.getServiceName();
        this.customerEmail = paymentDto.getCustomerEmail();
        this.customerName = paymentDto.getCustomerName();
        this.phoneNumber = paymentDto.getPhoneNumber();
        this.bankCode = paymentDto.getBankCode();
        this.network = paymentDto.getNetwork();

    }

    public Payment(PaymentDto paymentDto, BigDecimal amount) {
        this.billId = paymentDto.getBillId();
        this.responseId = paymentDto.getResponseId();
        this.paymentMethod = paymentDto.getPaymentMethod();
        this.status = PaymentStatus.PENDING; // never trust client-supplied status
        this.transRef = paymentDto.getTransRef();
        this.serviceName = paymentDto.getServiceName();
        this.customerEmail = paymentDto.getCustomerEmail();
        this.customerName = paymentDto.getCustomerName();
        this.phoneNumber = paymentDto.getPhoneNumber();
        this.bankCode = paymentDto.getBankCode();
        this.network = paymentDto.getNetwork();
        this.amountPaid = amount;
    }
}
