package com.mesh_suite.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnalyticsPayload {
    private final List<ProgramOption> programs;
    private final List<KpiValue> kpis;
    private final List<NamedCount> registered;
    private final List<NamedCount> gender;
    private final List<NamedCount> regions;
    private final List<NamedCount> sectors;
    private final List<NamedCount> disability;
    private final List<NamedCount> ownershipAge;
    private final List<NamedCount> age;
    private final List<NamedCount> literacy;
    private final List<NamedCount> active;
    private final List<MonthPoint> monthwise;

    public record ProgramOption(String id, String label) {}

    public record NamedCount(String name, long value) {}

    public static class KpiValue {
        private final String label;
        private final long value;
        private final boolean currency;

        public KpiValue(String label, long value, boolean currency) {
            this.label = label;
            this.value = value;
            this.currency = currency;
        }

        public String getLabel() {
            return label;
        }

        public long getValue() {
            return value;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("isCurrency")
        public boolean getIsCurrency() {
            return currency;
        }
    }

    public record MonthPoint(String month, long clients) {}
}
