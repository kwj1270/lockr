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
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class ClubService implements FoundClubUseCase, RegisterClubMemberUseCase, AssignMangerUseCase, AssignCoachUseCase {

    private final ClubRepository clubRepository;

    public ClubService(final ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    @Override
    public Club found(final FoundClubCommand command) {
        final Club existingClub = clubRepository.findByName(command.name());
        if (nonNull(existingClub)) {
            return existingClub;
        }
        final Club club = Club.init(
                command.userId(), command.name(), command.sportType(), command.city(),
                command.district(), command.description(), command.profileImageUrl(), command.backgroundImageUrl()
        );
        club.addMember(president(command.userId(), club.getId()));
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
        club.addMember(basic(command.userId(), club.getId()));
        return clubRepository.save(club);
    }

    @Override
    public Club assignCoach(final AssignCoachCommand command) {
        final Club club = club(command.clubId(), command.userId());
        if (club.isPresident(command.targetMemberId()) || club.hasNotMember(command.targetMemberId())) {
            throw new IllegalArgumentException();
        }
        club.assignCoach(command.targetMemberId());
        return clubRepository.save(club);
    }

    @Override
    public Club assignManager(final AssignManagerCommand command) {
        final Club club = club(command.clubId(), command.userId());
        if (club.isPresident(command.targetMemberId()) || club.hasNotMember(command.targetMemberId())) {
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
