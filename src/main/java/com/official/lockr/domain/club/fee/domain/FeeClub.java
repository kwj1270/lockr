package com.official.lockr.domain.club.fee.domain;

public interface FeeClub {

    boolean hasFeePermission(String clubId, String userId);

    void validateClubExists(String clubId);
}
