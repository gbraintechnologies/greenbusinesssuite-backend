package com.mesh_suite.dao.form;

import com.mesh_suite.domain.form.FormSections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormSectionRepository extends JpaRepository<FormSections, Long> {
}
