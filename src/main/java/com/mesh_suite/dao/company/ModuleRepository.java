package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByModuleNameContainingIgnoreCase(String moduleName);
}
