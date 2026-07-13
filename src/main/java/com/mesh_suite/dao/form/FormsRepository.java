
package com.mesh_suite.dao.form;

import com.mesh_suite.domain.form.Forms;
import com.mesh_suite.dto.FormProjection;
import com.mesh_suite.util.FormUtils;
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
public interface FormsRepository extends JpaRepository<Forms, Long> {
    Optional<Forms> findByName(String formName);
    Optional<Forms> findByIdAndIsDeletedFalse(Long id);

    Page<Forms> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
    Page<Forms> findAllByIsTemplateTrueAndIsDeletedFalse(Pageable pageable);

    Optional<Forms> findByIdAndPublishStatus(Long formId, FormUtils.PublishStatus publishStatus);

    long countByPublishStatusAndIsDeletedFalse(FormUtils.PublishStatus publishStatus);

    @Query("SELECT COUNT(f) FROM Forms f WHERE f.isDeleted = false AND f.publishStatus IN :statuses")
    long countByPublishStatusInAndIsDeletedFalse(@Param("statuses") List<FormUtils.PublishStatus> statuses);

    @Query("SELECT f.name FROM Forms f WHERE f.id = :formId")
    Optional<String> findNameByFormId(@Param("formId") Long formId);

    @Query("SELECT f FROM Forms f WHERE f.companyId IS NULL AND f.isDeleted = false")
    Page<Forms> findUnAssignForms(Pageable pageable);

    @Query("SELECT f.id FROM Forms f WHERE f.publishStatus = :publishStatus AND f.companyId = :companyId")
    List<Long> findPublishedFormIds(@Param("publishStatus") FormUtils.PublishStatus publishStatus, @Param("companyId") Long companyId);

    Page<Forms> findAllByIsTemplateTrueAndIsDeletedFalseAndCreatedOnAfter(LocalDateTime startDate, Pageable pageable);

    Page<Forms> findByCompanyIdAndCreatedOnAfter(Long companyId, LocalDateTime startDate, Pageable pageable);

    Page<Forms> findAllByIsTemplateFalseAndCreatedOnAfter(LocalDateTime startDate, Pageable pageable);

    Page<Forms> findAllByCreatedOnAfter(LocalDateTime startDate, Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.id IN :ids AND f.createdOn > :startDate")
    Page<Forms> findAllByIdAndCreatedOnAfter(@Param("ids") List<Long> ids, @Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.id IN :ids")
    Page<Forms> findAllById(@Param("ids") List<Long> ids, Pageable pageable);


    @Query("SELECT f FROM Forms f WHERE f.companyId IS NULL AND f.createdOn >= :startDate AND f.isDeleted = false")
    Page<Forms> findUnAssignFormsWithTimeline(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    List<Forms> findByIdIn(List<Long> formIds);

    Optional<String> findNameById(Long id);
    Page<Forms> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.companyId = :companyId AND f.isDeleted = false")
    Page<FormProjection> findByCompanyIdAndNotDeleted(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.companyId = :companyId AND f.createdOn >= :startDate AND f.isDeleted = false")
    Page<FormProjection> findByCompanyIdAndCreatedOnAfterAndNotDeleted(@Param("companyId") Long companyId, @Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.isTemplate = false AND f.isDeleted = false ORDER BY f.createdOn DESC, f.id DESC")
    Page<FormProjection> findAllNonDeletedNonTemplateForms(Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.isTemplate = false AND f.isDeleted = false AND f.createdOn >= :startDate ORDER BY f.createdOn DESC, f.id DESC")
    Page<FormProjection> findFilteredFormsByStartDate(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.isDeleted = false ORDER BY f.createdOn DESC")
    Page<FormProjection> findAllRecentForms(Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.isDeleted = false AND f.createdOn >= :startDate ORDER BY f.createdOn DESC")
    Page<FormProjection> findAllRecentFormsWithTimeline(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.isTemplate = true AND f.isDeleted = false ORDER BY f.createdOn DESC, f.id DESC")
    Page<FormProjection> findAllTemplatesNonDeleted(Pageable pageable);

    @Query("SELECT f FROM Forms f WHERE f.isTemplate = true AND f.isDeleted = false AND f.createdOn >= :startDate ORDER BY f.createdOn DESC, f.id DESC")
    Page<FormProjection> findAllTemplatesNonDeletedWithTimeline(@Param("startDate") LocalDateTime startDate, Pageable pageable);


}
