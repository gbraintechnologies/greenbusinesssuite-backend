package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    List<BusinessProfile> findByUserId(Long userId);
    @Query("SELECT b.gender, COUNT(b) FROM BusinessProfile b GROUP BY b.gender")
    List<Object[]> countByGender();

    @Query("SELECT b.typeOfBusiness, COUNT(b) FROM BusinessProfile b WHERE b.typeOfBusiness IS NOT NULL GROUP BY b.typeOfBusiness")
    List<Object[]> countByTypeOfBusiness();

    @Query("SELECT b.sector, COUNT(b) FROM BusinessProfile b WHERE b.sector IS NOT NULL GROUP BY b.sector")
    List<Object[]> countBySector();


}
