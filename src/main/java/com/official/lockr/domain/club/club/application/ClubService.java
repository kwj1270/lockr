package com.official.lockr.domain.club.club.application;

import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.application.command.AssignCoachCommand;
import com.official.lockr.domain.club.club.application.command.AssignManagerCommand;
import com.official.lockr.domain.club.club.application.command.FoundClubCommand;
import com.official.lockr.domain.club.club.application.command.UpdateMemberProfileImageCommand;
import com.official.lockr.domain.club.club.application.usecase.AssignCoachUseCase;
import com.official.lockr.domain.club.club.application.usecase.AssignMangerUseCase;
import com.official.lockr.domain.club.club.application.usecase.FoundClubUseCase;
import com.official.lockr.domain.club.club.application.usecase.RegisterClubMemberUseCase;
import com.official.lockr.domain.club.club.application.usecase.UpdateMemberProfileImageUseCase;
import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import org.springframework.stereotype.Service;

import static com.official.lockr.domain.club.club.domain.Member.basic;
import static com.official.lockr.domain.club.club.domain.Member.president;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class ClubService implements FoundClubUseCase, RegisterClubMemberUseCase, AssignMangerUseCase, AssignCoachUseCase, UpdateMemberProfileImageUseCase {

    private final ClubRepository clubRepository;
    private final UsersRepository usersRepository;

    public ClubService(final ClubRepository clubRepository, final UsersRepository usersRepository) {
        this.clubRepository = clubRepository;
        this.usersRepository = usersRepository;
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
        return clubRepository.save(club);
    }

    private String getFounderProfileImage(final String userId) {
        final Users user = usersRepository.findById(userId);
        if (isNull(user) || isNull(user.getUserAdditionalInfo())) {
            return null;
        }
        return user.getUserAdditionalInfo().getProfileImage();
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
        final String name = resolveName(command);
        final String profileImage = resolveProfileImage(command);
        if(command.memberRole().isPresident()) {
            club.addMember(president(command.userId(), club.getId(), name, profileImage));
        }
        if(command.memberRole().isBasic()) {
            club.addMember(basic(command.userId(), club.getId(), name, profileImage));
        }
        return clubRepository.save(club);
    }

    private String resolveName(final AddMemberCommand command) {
        if (nonNull(command.name())) {
            return command.name();
        }
        final Users user = usersRepository.findById(command.userId());
        if (isNull(user) || isNull(user.getUserAdditionalInfo())) {
            return null;
        }
        return user.name();
    }

    private String resolveProfileImage(final AddMemberCommand command) {
        if (nonNull(command.profileImage())) {
            return command.profileImage();
        }
        return getFounderProfileImage(command.userId());
    }

    @Override
    public Club assignCoach(final AssignCoachCommand command) {
        final Club club = club(command.clubId(), command.userId());
        if (club.isPresident(command.targetUserId()) || club.hasNotMember(command.targetUserId())) {
            throw new IllegalArgumentException();
        }
        club.assignCoach(command.targetUserId());
        return clubRepository.save(club);
    }

    @Override
    public Club assignManager(final AssignManagerCommand command) {
        final Club club = club(command.clubId(), command.userId());
        if (club.isPresident(command.targetUserId()) || club.hasNotMember(command.targetUserId())) {
            throw new IllegalArgumentException();
        }
        club.assignManger(command.targetUserId());
        return clubRepository.save(club);
    }

    private Club club(final String clubId, final String userId) {
        final Club club = clubRepository.findById(clubId);
        if (isNull(club) || !club.isStaff(userId)) {
            throw new IllegalArgumentException();
        }
        return club;
    }

    @Override
    public Club updateMemberProfileImage(final UpdateMemberProfileImageCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalStateException();
        }
        if (!club.isExistedUser(command.userId())) {
            throw new IllegalArgumentException();
        }
        club.updateMemberProfileImage(command.userId(), command.profileImage());
        return clubRepository.save(club);
    }
}
