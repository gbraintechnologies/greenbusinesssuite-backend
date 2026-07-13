package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.CategorySpecificModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorySpecificModuleRepository extends JpaRepository<CategorySpecificModule, Long> {
    List<CategorySpecificModule> findAllById(Iterable<Long> ids);
    Optional<CategorySpecificModule> findById(Long id);
}
