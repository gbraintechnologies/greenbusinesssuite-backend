package com.mesh_suite.dao.form;

import com.mesh_suite.constant.forms.FormResponseStatus;
import com.mesh_suite.domain.form.FormData;
import com.mesh_suite.domain.form.FormDataField;
import com.mesh_suite.dto.FormDataProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FormDataRepository extends JpaRepository<FormData, Long> {
    Optional<FormData> findByUserIdAndFormId(Long userId, Long formId);

    long countByIsCompleted(boolean isCompleted);

    long countByFormIdAndIsCompleted(Long formId, boolean isCompleted);

    List<FormData> findAllByUserIdAndCompanyIdAndFormId(Long userId, Long companyId, Long formId);

    List<FormData> findAllByUserIdAndFormId(Long userId, Long formId);

    List<FormData> findAllByUserIdAndCompanyId(Long userId, Long companyId);

    Long countByUserIdAndIsCompletedTrue(Long userId);

    Long countByUserIdAndIsCompletedFalse(Long userId);

    List<FormData> findByUserId(Long userId);

    @Query("SELECT fd.response FROM FormDataField fd " +
            "WHERE fd.formSection.inputData.formData.formId = :formId " +
            "AND fd.formFieldId = :formFieldId " +
            "AND fd.response IS NOT NULL")
    List<String> findFieldValuesByFormIdAndFieldId(@Param("formId") Long formId, @Param("formFieldId") Long formFieldId);

    @Query("SELECT fd.response, COUNT(fd) FROM FormDataField fd " +
            "WHERE fd.formSection.inputData.formData.formId = :formId " +
            "AND fd.formFieldId = :formFieldId GROUP BY fd.response")
    List<Object[]> countResponsesByFormIdAndFieldId(@Param("formId") Long formId, @Param("formFieldId") Long formFieldId);

    @Query("SELECT fd FROM FormDataField fd " +
            "WHERE fd.formSection.inputData.formData.formId = :formId " +
            "AND fd.formSection.inputData.formData.companyId = :companyId " +
            "AND fd.isStatisticalField = true")
    List<FormDataField> findStatsFieldsByFormIdAndCompanyId(@Param("formId") Long formId, @Param("companyId") Long companyId);
   @Query("SELECT fd FROM FormData fd WHERE fd.formId = :formId AND fd.companyId = :companyId")
   List<FormData> findByCompanyIdsAndFormId(@Param("companyId") Long companyId, @Param("formId") Long formId);

    @Query("SELECT COUNT(DISTINCT fd.userId) FROM FormData fd WHERE fd.companyId = :companyId")
    long countUniqueUserIdsByCompanyId(@Param("companyId") Long companyId);

    long countByCompanyId(Long companyId);

    long countByCompanyIdAndIsCompleted(Long companyId, boolean isCompleted);

    @Query("SELECT f.userId, COUNT(f) FROM FormData f WHERE f.companyId = :companyId AND f.isCompleted = true GROUP BY f.userId")
    Page<Object[]> findCompletedFormCountsByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT f.userId, COUNT(f) FROM FormData f WHERE f.companyId = :companyId AND f.isCompleted = true AND f.createdOn >= :startDate GROUP BY f.userId")
    Page<Object[]> findCompletedFormCountsByCompanyWithTimeline(@Param("companyId") Long companyId, @Param("startDate") LocalDateTime startDate, Pageable pageable);

    Page<FormData> findByCompanyIdAndStatus(Long companyId, FormResponseStatus status, Pageable pageable);
    @Query("SELECT f.formId FROM FormData f WHERE f.userId = :userId")
    List<Long> findFormIdsByUserId(@Param("userId") Long userId);

    List<FormData> findByUserIdAndIsCompleted(Long userId, Boolean isCompleted);

    @Query("SELECT f FROM FormData f WHERE f.createdOn >= :startDate ORDER BY f.createdOn DESC")
    Page<FormData> findAllWithTimeline(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT f FROM FormData f WHERE f.companyId = :companyId ORDER BY f.createdOn DESC")
    Page<FormData> findAllByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT f FROM FormData f WHERE f.companyId = :companyId AND f.createdOn >= :startDate ORDER BY f.createdOn DESC")
    Page<FormData> findAllByCompanyIdWithTimeline(@Param("companyId") Long companyId, @Param("startDate") LocalDateTime startDate, Pageable pageable);
    @Query("SELECT f FROM FormData f WHERE f.formId = :formId")
    Page<FormDataProjection> findAllByFormId(@Param("formId") Long formId, Pageable pageable);

    @Query("SELECT f FROM FormData f WHERE f.formId = :formId AND f.createdOn >= :startDate")
    Page<FormDataProjection> findAllByFormIdWithTimeline(@Param("formId") Long formId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         Pageable pageable);
}