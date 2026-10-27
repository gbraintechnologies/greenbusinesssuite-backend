package com.mesh_suite.dao.analytics;

import com.mesh_suite.domain.analytics.GeaAnalyticsStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeaAnalyticsStatRepository extends JpaRepository<GeaAnalyticsStat, Long> {

    boolean existsByScope(String scope);

    List<GeaAnalyticsStat> findByScopeAndProgramIdOrderByValueDesc(String scope, String programId);
}
