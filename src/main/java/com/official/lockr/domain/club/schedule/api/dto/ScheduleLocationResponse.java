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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ScheduleLocationResponse from(String locationJson) {
        if (locationJson == null || locationJson.isBlank()) {
            return new ScheduleLocationResponse(null, null, null, null, null, null, null, null);
        }

        try {
            // JSON 문자열을 파싱
            return objectMapper.readValue(locationJson, ScheduleLocationResponse.class);
        } catch (JsonProcessingException e) {
            // JSON 파싱 실패 시 name만 설정
            return new ScheduleLocationResponse(locationJson, null, null, null, null, null, null, null);
        }
    }
}
