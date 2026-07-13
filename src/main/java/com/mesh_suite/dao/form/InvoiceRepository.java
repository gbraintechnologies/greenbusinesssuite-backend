package com.mesh_suite.dao.form;

import com.mesh_suite.domain.form.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByBillId(Long billId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByCreatedOnAfter(LocalDateTime startDate, Pageable pageable);

    boolean existsByBillId(Long billId);
}
