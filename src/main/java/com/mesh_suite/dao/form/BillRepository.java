package com.mesh_suite.dao.form;

import com.mesh_suite.constant.forms.BillingType;
import com.mesh_suite.constant.forms.Status;
import com.mesh_suite.domain.form.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Page<Bill> findByCreatedOnAfter(LocalDateTime date, Pageable pageable);
    List<Bill> findByServiceNameContainingIgnoreCase(String serviceName);
    List<Bill> findByStatus(Status status);
    List<Bill> findByBillingType(BillingType billingType);

    Optional<Bill> findByFormId(Long formId);

    boolean existsByFormId(Long formId);
    List<Bill> findAllByFormId(Long formId);


}
