package com.mesh_suite.dao.country;

import com.mesh_suite.domain.coutry.AddressingScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressingSchemeRepository extends JpaRepository<AddressingScheme, Long> {}
