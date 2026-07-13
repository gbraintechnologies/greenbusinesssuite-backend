package com.mesh_suite.dao.form;

import com.mesh_suite.domain.form.FormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, Long> {
    @Query("SELECT ff.choiceValue FROM FormField ff JOIN ff.formSection fs JOIN fs.form f WHERE f.id = :formId")
    List<String> findChoiceValuesByFormId(@Param("formId") Long formId);
    List<FormField> findAllByIdIn(List<Long> ids);

}
