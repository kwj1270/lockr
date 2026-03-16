package com.official.lockr.domain.club.stats.api.dto;

import java.util.List;

public record MatchRecordsResponse(
        List<MatchRecordResponse> matchRecords
) {
}
