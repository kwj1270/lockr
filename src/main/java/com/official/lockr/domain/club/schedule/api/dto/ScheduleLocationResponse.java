package com.official.lockr.domain.club.schedule.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record ScheduleLocationResponse(
    String name,
    Double latitude,
    Double longitude,
    String address,
    String roadAddress,
    String placeId,
    String source,
    String category
) {
    public static ScheduleLocationResponse from(final String locationJson, final ObjectMapper objectMapper) {
        if (locationJson == null || locationJson.isBlank()) {
            return new ScheduleLocationResponse(null, null, null, null, null, null, null, null);
        }

        try {
            return objectMapper.readValue(locationJson, ScheduleLocationResponse.class);
        } catch (JsonProcessingException e) {
            return new ScheduleLocationResponse(locationJson, null, null, null, null, null, null, null);
        }
    }
}
