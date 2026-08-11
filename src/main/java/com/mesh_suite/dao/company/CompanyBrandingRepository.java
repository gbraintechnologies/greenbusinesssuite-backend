package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.CompanyBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyBrandingRepository extends JpaRepository<CompanyBranding, Long> {
    Optional<CompanyBranding> findByTenancyId(String tenancyId);
    Optional<CompanyBranding> findByCompanyId(Long companyId);
    void deleteByTenancyId(String tenancyId);
}
