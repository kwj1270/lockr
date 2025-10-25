package com.official.lockr.domain.club.squad.domain.squad;

import jakarta.annotation.Nullable;

public interface RecruitmentInfos {
    @Nullable RecruitmentInfo find(final String teamId, final String userId);
}
