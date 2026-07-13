package com.mesh_suite.dao.notify;

import com.mesh_suite.constant.notify.MediaType;
import com.mesh_suite.domain.notify.MediaCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MediaCenterRepository extends JpaRepository<MediaCenter,Long> {
    Page<MediaCenter> findByMediaType(MediaType mediaType, Pageable pageable);
    Page<MediaCenter> findAllByIsActive(Boolean isActive, Pageable pageable);
    List<MediaCenter> findByHeadingContainingIgnoreCaseAndMediaType(String heading, MediaType mediaType);

    List<MediaCenter> findByHeadingContainingIgnoreCase(String heading);

    @Query("SELECT m FROM MediaCenter m WHERE m.mediaType = :mediaType " +
            "AND m.createdOn >= :startDate AND m.createdOn < :endDate")
    Page<MediaCenter> findByMediaTypeAndDateRange(@Param("mediaType") MediaType mediaType,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  Pageable pageable);


}
