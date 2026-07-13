package com.mesh_suite.dao.form;


import com.mesh_suite.domain.form.DiscountedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountDataRepository extends JpaRepository<DiscountedData, Long> {
    List<DiscountedData> findByDiscountIdAndIsDeletedFalse(Long discountId);
    List<DiscountedData> findByDiscountId(Long discountId);
}
