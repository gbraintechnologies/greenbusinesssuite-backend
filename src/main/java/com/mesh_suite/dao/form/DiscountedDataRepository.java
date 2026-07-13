package com.mesh_suite.dao.form;

import com.mesh_suite.domain.form.DiscountedData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DiscountedDataRepository extends JpaRepository<DiscountedData, Long> {
    Page<DiscountedData> findByIsDeletedFalse(Pageable pageable);
    Page<DiscountedData> findByIsDeletedFalseAndCreatedOnAfter(LocalDateTime createdOn, Pageable pageable);

    Page<DiscountedData> findByServiceNameContainingIgnoreCaseAndCreatedOnAfter(String serviceName, LocalDateTime createdOn, Pageable pageable);
}
