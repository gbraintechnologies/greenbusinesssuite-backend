package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.dao.company.BusinessProfileRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.dto.response.AnalyticsPayload;
import com.mesh_suite.dto.response.AnalyticsPayload.KpiValue;
import com.mesh_suite.dto.response.AnalyticsPayload.MonthPoint;
import com.mesh_suite.dto.response.AnalyticsPayload.NamedCount;
import com.mesh_suite.dto.response.AnalyticsPayload.ProgramOption;
import com.mesh_suite.interceptor.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class AnalyticsService {

    private static final List<ProgramOption> ALL_PROGRAMS = List.of(new ProgramOption("all", "All programs"));

    private final BusinessProfileRepository businessProfileRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AnalyticsService(BusinessProfileRepository businessProfileRepository, UserRepository userRepository) {
        this.businessProfileRepository = businessProfileRepository;
        this.userRepository = userRepository;
    }

    public AnalyticsPayload generalBusiness() {
        return unscopedIfPlatform(() -> {
            long total = businessProfileRepository.count();
            long registered = businessProfileRepository.countRegistered();
            long nonRegistered = Math.max(0, total - registered);
            return AnalyticsPayload.builder()
                    .kpis(List.of(
                            new KpiValue("Total businesses", total, false),
                            new KpiValue("Total employees", 0, false)
                    ))
                    .registered(List.of(
                            new NamedCount("Registered", registered),
                            new NamedCount("Non-registered", nonRegistered)
                    ))
                    .gender(namedCounts(businessProfileRepository.countByGender()))
                    .regions(namedCounts(businessProfileRepository.countByAddress()))
                    .sectors(namedCounts(businessProfileRepository.countBySector()))
                    .disability(List.of())
                    .ownershipAge(List.of())
                    .literacy(List.of())
                    .build();
        });
    }

    public AnalyticsPayload loansAndGrants(String programId) {
        return emptyProgramAnalytics(
                programId,
                new KpiValue("Total beneficiaries", 0, false),
                new KpiValue("Total disbursed", 0, true)
        );
    }

    public AnalyticsPayload training(String programId) {
        return emptyProgramAnalytics(programId, new KpiValue("Total trainees", 0, false));
    }

    public AnalyticsPayload clients() {
        return unscopedIfPlatform(() -> {
            long total = userRepository.count();
            long active = userRepository.countByStatus(UserStatus.ACTIVE);
            long nonActive = Math.max(0, total - active);
            long registered = userRepository.countVerified();
            long nonRegistered = Math.max(0, total - registered);
            long newThisMonth = userRepository.countCreatedSince(LocalDate.now().withDayOfMonth(1).atStartOfDay());

            return AnalyticsPayload.builder()
                    .kpis(List.of(
                            new KpiValue("Total clients", total, false),
                            new KpiValue("New clients this month", newThisMonth, false)
                    ))
                    .active(List.of(
                            new NamedCount("Active", active),
                            new NamedCount("Non-active", nonActive)
                    ))
                    .registered(List.of(
                            new NamedCount("Registered", registered),
                            new NamedCount("Non-registered", nonRegistered)
                    ))
                    .gender(namedCounts(businessProfileRepository.countByGender()))
                    .age(List.of())
                    .regions(namedCounts(businessProfileRepository.countByAddress()))
                    .sectors(namedCounts(businessProfileRepository.countBySector()))
                    .monthwise(monthwiseClients())
                    .build();
        });
    }

    private AnalyticsPayload emptyProgramAnalytics(String programId, KpiValue... kpis) {
        return AnalyticsPayload.builder()
                .programs(ALL_PROGRAMS)
                .kpis(List.of(kpis))
                .registered(List.of())
                .gender(List.of())
                .regions(List.of())
                .sectors(List.of())
                .disability(List.of())
                .age(List.of())
                .build();
    }

    private List<MonthPoint> monthwiseClients() {
        LocalDate start = LocalDate.now().withDayOfYear(1);
        Map<Integer, Long> counts = new HashMap<>();
        for (Object[] row : userRepository.countCreatedByMonthSince(start.atStartOfDay())) {
            if (row[0] == null) {
                continue;
            }
            counts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        List<MonthPoint> points = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate cursor = start.withMonth(month);
            points.add(new MonthPoint(
                    cursor.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    counts.getOrDefault(month, 0L)
            ));
        }
        return points;
    }

    private List<NamedCount> namedCounts(List<Object[]> rows) {
        List<NamedCount> counts = new ArrayList<>();
        if (rows == null) {
            return counts;
        }
        for (Object[] row : rows) {
            counts.add(new NamedCount(label(row[0]), row[1] == null ? 0 : ((Number) row[1]).longValue()));
        }
        return counts;
    }

    private String label(Object value) {
        if (value == null) {
            return "Other / undisclosed";
        }
        String raw = String.valueOf(value).trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (raw.isEmpty()) {
            return "Other / undisclosed";
        }
        StringBuilder label = new StringBuilder();
        for (String part : raw.split(" ")) {
            if (part.isEmpty()) {
                continue;
            }
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                label.append(part.substring(1));
            }
        }
        return label.toString();
    }

    private <T> T unscopedIfPlatform(Supplier<T> query) {
        String tenant = TenantContext.getCurrentTenant();
        if (tenant != null && !AppConstants.DEFAULT_TENANT_ID.equals(tenant)) {
            return query.get();
        }
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("tenantFilter");
        try {
            return query.get();
        } finally {
            if (tenant != null) {
                session.enableFilter("tenantFilter").setParameter("tenantId", tenant);
            }
        }
    }
}
