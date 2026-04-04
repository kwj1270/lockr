package com.official.lockr.domain.club.fee.infrastructure;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.domain.club.fee.domain.FeeClub;
import org.springframework.stereotype.Repository;

@Repository
public class FeeClubAdapter implements FeeClub {

    private final ClubRepository clubRepository;

    public FeeClubAdapter(final ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    @Override
    public boolean hasFeePermission(final String clubId, final String userId) {
        final Club club = clubRepository.findById(clubId);
        if (club == null) {
            return false;
        }
        if (club.isPresidency(userId)) {
            return true;
        }
        return club.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .anyMatch(m -> m.getRole() == MemberRole.TREASURER);
    }

    @Override
    public void validateClubExists(final String clubId) {
        if (clubRepository.findById(clubId) == null) {
            throw new IllegalStateException("클럽을 찾을 수 없습니다.");
        }
    }
}
