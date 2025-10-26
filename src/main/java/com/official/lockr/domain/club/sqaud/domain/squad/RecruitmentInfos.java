package com.official.lockr.domain.club.sqaud.domain.squad;

import jakarta.annotation.Nullable;

public interface RecruitmentInfos {
    @Nullable RecruitmentInfo find(final String clubId, final String userId);
}
