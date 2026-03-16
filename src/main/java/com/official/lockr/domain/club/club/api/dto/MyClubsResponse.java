package com.official.lockr.domain.club.club.api.dto;

import java.util.List;

public record MyClubsResponse(
        List<MyClubResponse> clubs
) {
}