package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.dao.analytics.GeaProgramRecordRepository;
import com.mesh_suite.dao.analytics.GeaProgramRepository;
import com.mesh_suite.dao.company.BusinessProfileRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.analytics.GeaProgram;
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

import java.math.BigDecimal;
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

    private static final String LOAN_GRANT = "loan_grant";
    private static final String TRAINING = "training";
    private static final String BUSINESS = "business";
    private static final List<String> GENERAL_BUSINESS_TYPES = List.of(LOAN_GRANT, BUSINESS);

    private final BusinessProfileRepository businessProfileRepository;
    private final UserRepository userRepository;
    private final GeaProgramRepository geaProgramRepository;
    private final GeaProgramRecordRepository geaProgramRecordRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AnalyticsService(
            BusinessProfileRepository businessProfileRepository,
            UserRepository userRepository,
            GeaProgramRepository geaProgramRepository,
            GeaProgramRecordRepository geaProgramRecordRepository) {
        this.businessProfileRepository = businessProfileRepository;
        this.userRepository = userRepository;
        this.geaProgramRepository = geaProgramRepository;
        this.geaProgramRecordRepository = geaProgramRecordRepository;
    }

    public AnalyticsPayload generalBusiness() {
        return unscopedIfPlatform(() -> {
            long geaTotal = geaProgramRecordRepository.countByRecordTypes(GENERAL_BUSINESS_TYPES);
            if (geaTotal > 0) {
                return AnalyticsPayload.builder()
                        .kpis(List.of(
                                new KpiValue("Total businesses", geaTotal, false)
                        ))
                        .registered(namedCounts(geaProgramRecordRepository.countByRegisteredTypes(GENERAL_BUSINESS_TYPES)))
                        .gender(namedCounts(geaProgramRecordRepository.countByGenderTypes(GENERAL_BUSINESS_TYPES)))
                        .regions(namedCounts(geaProgramRecordRepository.countByRegionTypes(GENERAL_BUSINESS_TYPES)))
                        .sectors(namedCounts(geaProgramRecordRepository.countBySectorTypes(GENERAL_BUSINESS_TYPES)))
                        .disability(namedCounts(geaProgramRecordRepository.countByDisabilityTypes(GENERAL_BUSINESS_TYPES)))
                        .ownershipAge(namedCounts(geaProgramRecordRepository.countByAgeBandTypes(GENERAL_BUSINESS_TYPES)))
                        .literacy(namedCounts(geaProgramRecordRepository.countByLiteracyTypes(GENERAL_BUSINESS_TYPES)))
                        .build();
            }

            long total = businessProfileRepository.count();
            long registered = businessProfileRepository.countRegistered();
            long nonRegistered = Math.max(0, total - registered);
            return AnalyticsPayload.builder()
                    .kpis(List.of(
                            new KpiValue("Total businesses", total, false)
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
        String pid = normalizeProgramId(programId);
        return AnalyticsPayload.builder()
                .programs(programOptions(LOAN_GRANT))
                .kpis(List.of(
                        new KpiValue("Total beneficiaries",
                                geaProgramRecordRepository.countForProgram(LOAN_GRANT, pid), false),
                        new KpiValue("Total disbursed",
                                toLongAmount(geaProgramRecordRepository.sumApproved(LOAN_GRANT, pid)), true)
                ))
                .registered(namedCounts(geaProgramRecordRepository.countByRegistered(LOAN_GRANT, pid)))
                .gender(namedCounts(geaProgramRecordRepository.countByGender(LOAN_GRANT, pid)))
                .regions(namedCounts(geaProgramRecordRepository.countByRegion(LOAN_GRANT, pid)))
                .sectors(namedCounts(geaProgramRecordRepository.countBySector(LOAN_GRANT, pid)))
                .disability(namedCounts(geaProgramRecordRepository.countByDisability(LOAN_GRANT, pid)))
                .age(namedCounts(geaProgramRecordRepository.countByAgeBand(LOAN_GRANT, pid)))
                .build();
    }

    public AnalyticsPayload training(String programId) {
        String pid = normalizeProgramId(programId);
        return AnalyticsPayload.builder()
                .programs(programOptions(TRAINING))
                .kpis(List.of(
                        new KpiValue("Total trainees",
                                geaProgramRecordRepository.countForProgram(TRAINING, pid), false)
                ))
                .registered(namedCounts(geaProgramRecordRepository.countByRegistered(TRAINING, pid)))
                .gender(namedCounts(geaProgramRecordRepository.countByGender(TRAINING, pid)))
                .regions(namedCounts(geaProgramRecordRepository.countByRegion(TRAINING, pid)))
                .sectors(namedCounts(geaProgramRecordRepository.countBySector(TRAINING, pid)))
                .disability(namedCounts(geaProgramRecordRepository.countByDisability(TRAINING, pid)))
                .age(namedCounts(geaProgramRecordRepository.countByAgeBand(TRAINING, pid)))
                .build();
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

    private List<ProgramOption> programOptions(String kind) {
        List<ProgramOption> options = new ArrayList<>();
        options.add(new ProgramOption("all", "All programs"));
        for (GeaProgram program : geaProgramRepository.findByKindAndActiveTrueOrderByNameAsc(kind)) {
            options.add(new ProgramOption(program.getId(), program.getName()));
        }
        return options;
    }

    private String normalizeProgramId(String programId) {
        if (programId == null || programId.isBlank() || "all".equalsIgnoreCase(programId)) {
            return "all";
        }
        return programId.trim();
    }

    private long toLongAmount(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
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
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            return "Other / undisclosed";
        }
        // Preserve already-title-cased analytics labels from ETL
        if (raw.contains("–") || raw.contains("/") || Character.isUpperCase(raw.charAt(0))) {
            return raw;
        }
        String normalized = raw.toLowerCase(Locale.ROOT).replace('_', ' ');
        StringBuilder label = new StringBuilder();
        for (String part : normalized.split(" ")) {
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
