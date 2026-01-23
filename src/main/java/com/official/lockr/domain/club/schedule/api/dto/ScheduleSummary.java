package com.official.lockr.domain.club.schedule.api.dto;

import java.util.Map;

public record ScheduleSummary(
    int totalSchedules,
    Map<String, Integer> byType
) {
}
