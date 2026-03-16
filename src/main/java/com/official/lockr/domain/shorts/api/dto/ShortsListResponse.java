package com.official.lockr.domain.shorts.api.dto;

import java.util.List;

public record ShortsListResponse(
        List<ShortsItemResponse> shorts
) {
}
