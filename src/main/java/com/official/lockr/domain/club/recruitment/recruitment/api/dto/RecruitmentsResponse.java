package com.official.lockr.domain.club.recruitment.recruitment.api.dto;

import java.util.List;

public record RecruitmentsResponse(
        List<RecruitmentResponse> recruitments
) {
}
