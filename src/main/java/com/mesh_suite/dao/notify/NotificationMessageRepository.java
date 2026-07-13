package com.mesh_suite.dao.notify;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.domain.notify.NotificationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, Long> {
    Page<NotificationMessage> findByRecurringType(CompanyStatus.RecurringType recurringType, Pageable pageable);

    Page<NotificationMessage> findByRecurringTypeNot(CompanyStatus.RecurringType recurringType, Pageable pageable);

    @Query("SELECT n FROM NotificationMessage n " +
            "WHERE n.recurringType IN :recurringTypes " +
            "AND n.triggerTime BETWEEN :now AND :timeWindow " +
            "AND  n.endDate > :timeWindow")
    List<NotificationMessage> findValidNotifications(
            @Param("recurringTypes") List<CompanyStatus.RecurringType> recurringTypes,
            @Param("now") LocalDateTime now,
            @Param("timeWindow") LocalDateTime timeWindow
    );



}
