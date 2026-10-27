package com.mesh_suite.dao.analytics;

import com.mesh_suite.domain.analytics.GeaProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeaProgramRepository extends JpaRepository<GeaProgram, String> {
    List<GeaProgram> findByKindAndActiveTrueOrderByNameAsc(String kind);
}
