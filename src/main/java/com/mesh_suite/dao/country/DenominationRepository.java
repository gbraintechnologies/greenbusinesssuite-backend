package com.mesh_suite.dao.country;

import com.mesh_suite.domain.form.Denomination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface DenominationRepository extends JpaRepository<Denomination, Long> {
}
