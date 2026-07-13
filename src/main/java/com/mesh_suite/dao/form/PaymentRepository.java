package com.mesh_suite.dao.form;

import com.mesh_suite.constant.forms.PaymentMethod;
import com.mesh_suite.domain.form.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByDatePaidAfter(LocalDateTime datePaid, Pageable pageable);

    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.status = 'SUCCESSFUL'")
    BigDecimal findTotalRevenue();

    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.datePaid >= :startDate AND p.datePaid <= :endDate AND p.status = 'SUCCESSFUL'")
    BigDecimal findTotalRevenueWithinDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    List<Payment> findByServiceNameContainingIgnoreCase(String serviceName);
    List<Payment> findByCustomerNameContainingIgnoreCase(String customerName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.transRef = :transRef")
    Optional<Payment> findByTransRefForUpdate(@Param("transRef") String transRef);

    Optional<Payment> findByResponseId(Long responseId);
    
    Optional<Payment> findByTransactionId(String transactionId);

}
