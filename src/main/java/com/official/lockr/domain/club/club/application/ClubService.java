package com.official.lockr.domain.club.club.application;

import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.application.command.AssignCoachCommand;
import com.official.lockr.domain.club.club.application.command.AssignManagerCommand;
import com.official.lockr.domain.club.club.application.command.FoundClubCommand;
import com.official.lockr.domain.club.club.application.usecase.AssignCoachUseCase;
import com.official.lockr.domain.club.club.application.usecase.AssignMangerUseCase;
import com.official.lockr.domain.club.club.application.usecase.FoundClubUseCase;
import com.official.lockr.domain.club.club.application.usecase.RegisterClubMemberUseCase;
import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import org.springframework.stereotype.Service;

import static com.official.lockr.domain.club.club.domain.Member.basic;
import static com.official.lockr.domain.club.club.domain.Member.president;
import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ClubService implements FoundClubUseCase, RegisterClubMemberUseCase, AssignMangerUseCase, AssignCoachUseCase {

    private final ClubRepository clubRepository;

    public ClubService(final ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    @Override
    public Club found(final FoundClubCommand command) {
        final Club club = Club.init(
                generateUlid(), command.userId(), command.name(), command.description(), command.region(),
                command.sportType(), command.profileImageUrl(), command.backgroundImageUrl()
        );
        club.addMember(president(generateUlid(), command.userId(), club.getId()));
        return clubRepository.save(club);
    }

    @Override
    public Club addMember(final AddMemberCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalStateException();
        }
        if (club.isExistedUser(command.userId())) {
            return club;
        }
        club.addMember(basic(generateUlid(), command.userId(), club.getId()));
        return clubRepository.save(club);
    }

    @Override
    public Club assignCoach(final AssignCoachCommand command) {
        final Club club = club(command.clubId(), command.userId());
        if (club.isNotPresident(command.targetMemberId()) || club.hasNotMember(command.targetMemberId())) {
            throw new IllegalArgumentException();
        }
        club.assignCoach(command.targetMemberId());
        return clubRepository.save(club);
    }

    @Override
    public Club assignManager(final AssignManagerCommand command) {
        final Club club = club(command.clubId(), command.userId());
        if (club.isNotPresident(command.targetMemberId()) || club.hasNotMember(command.targetMemberId())) {
            throw new IllegalArgumentException();
        }
        club.assignManger(command.targetMemberId());
        return clubRepository.save(club);
    }

    private Club club(final String clubId, final String userId) {
        final Club club = clubRepository.findById(clubId);
        if (isNull(club) || !club.isStaff(userId)) {
            throw new IllegalArgumentException();
        }
        return club;
    }
}
