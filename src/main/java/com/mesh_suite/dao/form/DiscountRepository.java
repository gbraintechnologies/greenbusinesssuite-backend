package com.mesh_suite.dao.form;

import com.mesh_suite.domain.form.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Page<Discount> findAll(Pageable pageable);

    Optional<Discount> findByIdAndIsActiveTrue(Long id);

    List<Discount> findByIsActive(boolean isActive);

    Page<Discount> findByCreatedOnAfter(LocalDateTime createdAfter, Pageable pageable);
}
