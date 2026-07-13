package com.mesh_suite.service.notify;

import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.domain.company.UserCompany;
import com.mesh_suite.interceptor.TenantContext;
import com.mesh_suite.producer.NotificationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
public class NotificationScheduler {

    private final NotificationProcessor notificationProcessor;
    private final UserCompanyRepository companyRepository;

    public NotificationScheduler(NotificationProcessor notificationProcessor,
                                 UserCompanyRepository companyRepository) {
        this.notificationProcessor = notificationProcessor;
        this.companyRepository = companyRepository;
    }

    @Scheduled(fixedRate = 600000) // Executes every 10 minutes
    public void triggerScheduledNotifications() {
        log.info("Starting notification scheduler...");

        // Fetch all active companies
        List<UserCompany> activeCompanies = companyRepository.findAllByStatus(CompanyStatus.ACTIVE);
        if (activeCompanies.isEmpty()) {
            log.info("No active companies found for notification processing.");
            return;
        }

        log.info("Found {} active companies for processing.", activeCompanies.size());

        activeCompanies.forEach(company -> {
            String tenantId = company.getCompanyIdentifier();
            log.debug("Processing notifications for tenant: {}", tenantId);
            try {
                // Set tenant context explicitly for each iteration
                TenantContext.setCurrentTenant(tenantId);
                notificationProcessor.processNotificationsForTenant(tenantId);
                log.debug("Finished processing notifications for tenant: {}", tenantId);
            } catch (Exception e) {
                log.error("Error processing notifications for tenant: {}", tenantId, e);
            } finally {
                TenantContext.clear(); // it's cleared for the next iteration
            }
        });

        log.info("Completed notification processing for all companies.");
    }
}
