package com.official.lockr.domain.club.feed.api.dto;

import java.util.List;

public record HeartsResponse(
    List<HeartItemResponse> hearts
) {
}
