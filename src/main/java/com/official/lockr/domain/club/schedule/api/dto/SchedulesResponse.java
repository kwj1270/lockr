package com.official.lockr.domain.club.schedule.api.dto;

import java.util.List;

public record SchedulesResponse(
    List<ScheduleItemResponse> schedules
) {
}
