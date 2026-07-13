package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.CategorySetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorySetupRepository extends JpaRepository<CategorySetup, Long> {
    List<CategorySetup> findByCategoryNameContainingIgnoreCase(String categoryName);
}
