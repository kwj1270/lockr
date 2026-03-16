package com.official.lockr.domain.club.schedule.api.dto;

import java.util.List;

public record ScheduleCommentsResponse(
        List<ScheduleCommentItemResponse> comments
) {
}
