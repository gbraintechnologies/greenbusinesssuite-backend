package com.mesh_suite.dao.country;

import com.mesh_suite.domain.coutry.ParentLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentLevelRepository extends JpaRepository<ParentLevel, Long> {
    @Query("SELECT p.childLevels FROM ParentLevel p " +
            "JOIN p.addressingScheme a " +
            "JOIN a.country c " +
            "WHERE p.parentName = :parentName AND c.id = :countryId")
    List<String> findChildLevelsByParentNameAndCountryId(
            @Param("parentName") String parentName,
            @Param("countryId") Long countryId
    );

    @Query("SELECT p FROM ParentLevel p WHERE LOWER(p.parentName) = LOWER(:parentName)")
    Optional<ParentLevel> findByParentNameIgnoreCase(@Param("parentName") String parentName);
}