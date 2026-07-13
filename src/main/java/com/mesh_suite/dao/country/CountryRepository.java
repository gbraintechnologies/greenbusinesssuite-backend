package com.mesh_suite.dao.country;

import com.mesh_suite.domain.coutry.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCountryId(Long countryId);

    @Query("SELECT c FROM Country c " +
            "JOIN FETCH c.addressingScheme a " +
            "JOIN FETCH a.parentLevels p " +
            "JOIN FETCH p.childLevels " +
            "WHERE c.id = :id")
    Optional<Country> findByIdWithDetails(@Param("id") Long id);

    Optional<Country> findByCountryNameIgnoreCase(String countryName);

    Optional<Country> findByCountryName(String countryName);

}