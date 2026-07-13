package com.mesh_suite.util;

import com.mesh_suite.constant.forms.Timeline;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class TimelineFilter {
    public static LocalDateTime calculateStartDate(Timeline timeline) {
        LocalDateTime now = LocalDateTime.now();
        return switch (timeline) {
            case TODAY -> now.toLocalDate().atStartOfDay();
            case THIS_WEEK -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case THIS_MONTH -> now.with(TemporalAdjusters.firstDayOfMonth());
            case THIS_YEAR -> now.with(TemporalAdjusters.firstDayOfYear());
            default -> throw new IllegalArgumentException("Invalid timeline");
        };
    }
}
