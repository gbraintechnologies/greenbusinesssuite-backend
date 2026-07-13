package com.mesh_suite.dao.country;

import com.mesh_suite.domain.form.CurrencySetup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencySetupRepository extends JpaRepository<CurrencySetup, Long> {
    Page<CurrencySetup> findByIsDeletedFalse(Pageable pageable);

    List<CurrencySetup> findByCountryNameContainingIgnoreCase(String countryName);

    @Query("SELECT cs FROM CurrencySetup cs WHERE LOWER(cs.currency) = LOWER(:currency)")
    List<CurrencySetup> findByCurrencyIgnoreCase(@Param("currency") String currency);

    @Query("SELECT c FROM CurrencySetup c WHERE LOWER(c.currency) = LOWER(:currencyName)")
    Optional<CurrencySetup> findCurrencyByNameIgnoreCase(@Param("currencyName") String currencyName);


}
