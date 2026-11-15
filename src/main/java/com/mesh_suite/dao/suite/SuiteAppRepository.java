package com.mesh_suite.dao.suite;

import com.mesh_suite.domain.suite.SuiteApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuiteAppRepository extends JpaRepository<SuiteApp, Long> {

    List<SuiteApp> findByPublishedTrueOrderBySortOrderAscIdAsc();

    List<SuiteApp> findAllByOrderBySortOrderAscIdAsc();
}
