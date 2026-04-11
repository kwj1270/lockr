package com.official.lockr.domain.club.fee.domain;

import java.util.List;

public interface FeeClub {

    boolean hasFeePermission(String clubId, String userId);

    void validateClubExists(String clubId);

    List<String> findUserIdsByMemberIds(String clubId, List<String> memberIds);
}
