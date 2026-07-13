package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.SectorSetup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectorSetupRepository extends JpaRepository<SectorSetup,Long> {
    @Query("SELECT ss.id, ss.countryName, " +
            "(SELECT COUNT(s) FROM ss.sectors s) AS parentSectorCount, " +
            "(SELECT COUNT(subS) FROM ss.sectors s LEFT JOIN s.subSector subS) AS subSectorCount " +
            "FROM SectorSetup ss " +
            "WHERE LOWER(ss.countryName) = LOWER(:countryName) " +
            "GROUP BY ss.id, ss.countryName")
    List<Object[]> findSectorStatisticsByCountryName(String countryName);
    @Query("SELECT ss FROM SectorSetup ss WHERE LOWER(ss.countryName) = LOWER(:countryName)")

    List<SectorSetup> findByCountryNameIgnoreCase(@Param("countryName") String countryName);
    Optional<SectorSetup> findByCountryName(String countryName);

    @Query("SELECT ss.id, ss.countryName, COUNT(DISTINCT s.id) AS parentSectorCount, COALESCE(COUNT(subS), 0) AS subSectorCount " +
            "FROM SectorSetup ss " +
            "LEFT JOIN ss.sectors s " +
            "LEFT JOIN s.subSector subS " +
            "GROUP BY ss.id, ss.countryName")
    Page<Object[]> findAllSectorSetupAggregates(Pageable pageable);

    @Query("SELECT s FROM SectorSetup s WHERE LOWER(s.countryName) = LOWER(:countryName)")
    Optional<SectorSetup> findSectorByCountryName(@Param("countryName") String countryName);

}
