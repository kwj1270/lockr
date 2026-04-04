package com.official.lockr.domain.club.fee.domain;

import jakarta.annotation.Nullable;

public interface FeePolicyRepository {
    FeePolicy save(FeePolicy feePolicy);

    @Nullable
    FeePolicy findByClubId(String clubId);
}
