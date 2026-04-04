package com.official.lockr.domain.club.fee.domain;

public interface FeePolicyRepository {
    FeePolicy save(FeePolicy feePolicy);

    /** @return null if not found */
    FeePolicy findByClubId(String clubId);
}
