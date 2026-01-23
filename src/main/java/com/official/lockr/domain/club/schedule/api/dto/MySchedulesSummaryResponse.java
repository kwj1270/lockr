package com.official.lockr.domain.club.schedule.api.dto;

import java.util.List;
import java.util.Map;

public record MySchedulesSummaryResponse(
    int year,
    int month,
    ScheduleSummary summary,
    List<MyScheduleItemResponse> schedules
) {
}
