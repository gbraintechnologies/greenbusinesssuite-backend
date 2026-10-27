package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.dao.analytics.GeaAnalyticsStatRepository;
import com.mesh_suite.dao.analytics.GeaProgramRecordRepository;
import com.mesh_suite.dao.analytics.GeaProgramRepository;
import com.mesh_suite.dao.company.BusinessProfileRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.analytics.GeaAnalyticsStat;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class AnalyticsService {

    private static final String LOAN_GRANT = "loan_grant";
    private static final String TRAINING = "training";
    private static final String BUSINESS = "business";
    private static final String GENERAL = "general";
    private static final List<String> GENERAL_BUSINESS_TYPES = List.of(LOAN_GRANT, BUSINESS);
    private static final long CACHE_TTL_MS = 30 * 60 * 1000L;

    private final BusinessProfileRepository businessProfileRepository;
    private final UserRepository userRepository;
    private final GeaProgramRepository geaProgramRepository;
    private final GeaProgramRecordRepository geaProgramRecordRepository;
    private final GeaAnalyticsStatRepository geaAnalyticsStatRepository;
    private final ConcurrentHashMap<String, CacheEntry> responseCache = new ConcurrentHashMap<>();

    @PersistenceContext
    private EntityManager entityManager;

    public AnalyticsService(
            BusinessProfileRepository businessProfileRepository,
            UserRepository userRepository,
            GeaProgramRepository geaProgramRepository,
            GeaProgramRecordRepository geaProgramRecordRepository,
            GeaAnalyticsStatRepository geaAnalyticsStatRepository) {
        this.businessProfileRepository = businessProfileRepository;
        this.userRepository = userRepository;
        this.geaProgramRepository = geaProgramRepository;
        this.geaProgramRecordRepository = geaProgramRecordRepository;
        this.geaAnalyticsStatRepository = geaAnalyticsStatRepository;
    }

    public AnalyticsPayload generalBusiness() {
        return cached("general-business", () -> unscopedIfPlatform(() -> {
            AnalyticsPayload fromStats = fromGeneralStats();
            if (fromStats != null) {
                return fromStats;
            }

            long geaTotal = geaProgramRecordRepository.countDistinctBusinesses(GENERAL_BUSINESS_TYPES);
            if (geaTotal > 0) {
                return AnalyticsPayload.builder()
                        .kpis(List.of(new KpiValue("Total businesses", geaTotal, false)))
                        .registered(namedCounts(geaProgramRecordRepository.countDistinctByRegisteredTypes(GENERAL_BUSINESS_TYPES)))
                        .gender(namedCounts(geaProgramRecordRepository.countDistinctByGenderTypes(GENERAL_BUSINESS_TYPES)))
                        .regions(namedCounts(geaProgramRecordRepository.countDistinctByRegionTypes(GENERAL_BUSINESS_TYPES)))
                        .sectors(namedCounts(geaProgramRecordRepository.countDistinctBySectorTypes(GENERAL_BUSINESS_TYPES)))
                        .disability(namedCounts(geaProgramRecordRepository.countDistinctByDisabilityTypes(GENERAL_BUSINESS_TYPES)))
                        .ownershipAge(namedCounts(geaProgramRecordRepository.countDistinctByAgeBandTypes(GENERAL_BUSINESS_TYPES)))
                        .literacy(namedCounts(geaProgramRecordRepository.countDistinctByLiteracyTypes(GENERAL_BUSINESS_TYPES)))
                        .build();
            }

            long total = businessProfileRepository.count();
            long registered = businessProfileRepository.countRegistered();
            long nonRegistered = Math.max(0, total - registered);
            return AnalyticsPayload.builder()
                    .kpis(List.of(new KpiValue("Total businesses", total, false)))
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
        }));
    }

    public AnalyticsPayload loansAndGrants(String programId) {
        String pid = normalizeProgramId(programId);
        return cached("loans-grants:" + pid, () -> {
            AnalyticsPayload fromStats = fromProgramStats(LOAN_GRANT, pid, true);
            if (fromStats != null) {
                return fromStats;
            }
            return AnalyticsPayload.builder()
                    .programs(programOptions(LOAN_GRANT))
                    .kpis(List.of(
                            new KpiValue("Total beneficiaries",
                                    geaProgramRecordRepository.countDistinctForProgram(LOAN_GRANT, pid), false),
                            new KpiValue("Total disbursed",
                                    toLongAmount(geaProgramRecordRepository.sumApproved(LOAN_GRANT, pid)), true)
                    ))
                    .registered(namedCounts(geaProgramRecordRepository.countDistinctByRegistered(LOAN_GRANT, pid)))
                    .gender(namedCounts(geaProgramRecordRepository.countDistinctByGender(LOAN_GRANT, pid)))
                    .regions(namedCounts(geaProgramRecordRepository.countDistinctByRegion(LOAN_GRANT, pid)))
                    .sectors(namedCounts(geaProgramRecordRepository.countDistinctBySector(LOAN_GRANT, pid)))
                    .disability(namedCounts(geaProgramRecordRepository.countDistinctByDisability(LOAN_GRANT, pid)))
                    .age(namedCounts(geaProgramRecordRepository.countDistinctByAgeBand(LOAN_GRANT, pid)))
                    .build();
        });
    }

    public AnalyticsPayload training(String programId) {
        String pid = normalizeProgramId(programId);
        return cached("training:" + pid, () -> {
            AnalyticsPayload fromStats = fromProgramStats(TRAINING, pid, false);
            if (fromStats != null) {
                return fromStats;
            }
            return AnalyticsPayload.builder()
                    .programs(programOptions(TRAINING))
                    .kpis(List.of(
                            new KpiValue("Total trainees",
                                    geaProgramRecordRepository.countDistinctForProgram(TRAINING, pid), false)
                    ))
                    .registered(namedCounts(geaProgramRecordRepository.countDistinctByRegistered(TRAINING, pid)))
                    .gender(namedCounts(geaProgramRecordRepository.countDistinctByGender(TRAINING, pid)))
                    .regions(namedCounts(geaProgramRecordRepository.countDistinctByRegion(TRAINING, pid)))
                    .sectors(namedCounts(geaProgramRecordRepository.countDistinctBySector(TRAINING, pid)))
                    .disability(namedCounts(geaProgramRecordRepository.countDistinctByDisability(TRAINING, pid)))
                    .age(namedCounts(geaProgramRecordRepository.countDistinctByAgeBand(TRAINING, pid)))
                    .build();
        });
    }

    public AnalyticsPayload clients() {
        return cached("clients", () -> unscopedIfPlatform(() -> {
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
        }));
    }

    private AnalyticsPayload fromGeneralStats() {
        if (!geaAnalyticsStatRepository.existsByScope(GENERAL)) {
            return null;
        }
        List<GeaAnalyticsStat> rows = geaAnalyticsStatRepository.findByScopeAndProgramIdOrderByValueDesc(GENERAL, "all");
        if (rows.isEmpty()) {
            return null;
        }
        long total = scalar(rows, "total");
        return AnalyticsPayload.builder()
                .kpis(List.of(new KpiValue("Total businesses", total, false)))
                .registered(dimension(rows, "registered"))
                .gender(dimension(rows, "gender"))
                .regions(dimension(rows, "region"))
                .sectors(dimension(rows, "sector"))
                .disability(dimension(rows, "disability"))
                .ownershipAge(dimension(rows, "age"))
                .literacy(dimension(rows, "literacy"))
                .build();
    }

    private AnalyticsPayload fromProgramStats(String scope, String programId, boolean includeDisbursed) {
        if (!geaAnalyticsStatRepository.existsByScope(scope)) {
            return null;
        }
        List<GeaAnalyticsStat> rows = geaAnalyticsStatRepository.findByScopeAndProgramIdOrderByValueDesc(scope, programId);
        if (rows.isEmpty()) {
            return null;
        }
        List<KpiValue> kpis = new ArrayList<>();
        String totalLabel = LOAN_GRANT.equals(scope) ? "Total beneficiaries" : "Total trainees";
        kpis.add(new KpiValue(totalLabel, scalar(rows, "total"), false));
        if (includeDisbursed) {
            kpis.add(new KpiValue("Total disbursed", scalar(rows, "disbursed"), true));
        }
        return AnalyticsPayload.builder()
                .programs(programOptions(scope))
                .kpis(kpis)
                .registered(dimension(rows, "registered"))
                .gender(dimension(rows, "gender"))
                .regions(dimension(rows, "region"))
                .sectors(dimension(rows, "sector"))
                .disability(dimension(rows, "disability"))
                .age(dimension(rows, "age"))
                .build();
    }

    private long scalar(List<GeaAnalyticsStat> rows, String dimension) {
        return rows.stream()
                .filter(r -> dimension.equals(r.getDimension()))
                .map(GeaAnalyticsStat::getValue)
                .findFirst()
                .map(this::toLongAmount)
                .orElse(0L);
    }

    private List<NamedCount> dimension(List<GeaAnalyticsStat> rows, String dimension) {
        List<NamedCount> counts = new ArrayList<>();
        for (GeaAnalyticsStat row : rows) {
            if (!dimension.equals(row.getDimension())) {
                continue;
            }
            counts.add(new NamedCount(label(row.getName()), toLongAmount(row.getValue())));
        }
        return counts;
    }

    private AnalyticsPayload cached(String key, Supplier<AnalyticsPayload> loader) {
        CacheEntry existing = responseCache.get(key);
        long now = System.currentTimeMillis();
        if (existing != null && now - existing.loadedAtMs < CACHE_TTL_MS) {
            return existing.payload;
        }
        AnalyticsPayload payload = loader.get();
        responseCache.put(key, new CacheEntry(payload, now));
        return payload;
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

    private record CacheEntry(AnalyticsPayload payload, long loadedAtMs) {
    }
}
