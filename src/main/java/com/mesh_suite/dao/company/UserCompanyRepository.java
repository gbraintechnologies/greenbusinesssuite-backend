package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.UserCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import com.mesh_suite.constant.company.CompanyStatus;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCompanyRepository extends JpaRepository<UserCompany, Long> {

    Page<UserCompany> findByCompanyAdmin_Id(Long userId, Pageable pageable);

    Page<UserCompany> findByCompanyAdmin_CompanyIdentifier(String companyIdentifier, Pageable pageable);
    
    // Company-specific methods
    UserCompany findByCompanyNameContainingIgnoreCase(String companyName);
    
    @Query("SELECT uc FROM UserCompany uc WHERE uc.status = :status")
    Page<UserCompany> findByStatus(@Param("status") CompanyStatus status, Pageable pageable);
    
    // Additional useful company queries
    Optional<UserCompany> findByCompanyCode(String companyCode);
    Optional<UserCompany> findByPrimaryContactEmail(String email);
    
    @Query("SELECT uc FROM UserCompany uc WHERE " +
           "LOWER(uc.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(uc.primaryContactName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<UserCompany> searchByNameOrContact(@Param("searchTerm") String searchTerm, Pageable pageable);



    @Query("SELECT c FROM UserCompany c WHERE c.status = :status")
    List<UserCompany> findAllByStatus(@Param("status") CompanyStatus status);

    @Query("SELECT c FROM UserCompany c WHERE c.status = :status AND c.companyIdentifier = :identifier")
    Optional<UserCompany> findActiveByCompanyIdentifier(@Param("identifier") String companyIdentifier,
                                                        @Param("status") CompanyStatus status);
    @Query("SELECT c FROM UserCompany c WHERE c.id = :companyId")
    Optional<UserCompany> findCompanyDetailsById(@Param("companyId") Long companyId);

    Optional<UserCompany> findByCompanyIdentifier(String companyIdentifier);

    boolean existsByCompanyIdentifier(String companyIdentifier);

    Optional<UserCompany> findByCompanyName(String companyName);
}