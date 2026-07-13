package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.SectorSetup;
import com.mesh_suite.domain.company.Sectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectorsRepository extends JpaRepository<Sectors, Long> {
    void deleteAllBySectorSetup(SectorSetup sectorSetup);
}
