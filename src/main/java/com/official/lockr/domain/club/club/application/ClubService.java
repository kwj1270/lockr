package com.official.lockr.domain.club.club.application;

import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.application.command.AssignCoachCommand;
import com.official.lockr.domain.club.club.application.command.AssignManagerCommand;
import com.official.lockr.domain.club.club.application.command.ChangeJoinMethodCommand;
import com.official.lockr.domain.club.club.application.command.ChangeMemberRoleCommand;
import com.official.lockr.domain.club.club.application.command.ChangeVisibilityCommand;
import com.official.lockr.domain.club.club.application.command.DelegatePresidentCommand;
import com.official.lockr.domain.club.club.application.command.FoundClubCommand;
import com.official.lockr.domain.club.club.application.command.LeaveClubCommand;
import com.official.lockr.domain.club.club.application.command.UpdateClubCommand;
import com.official.lockr.domain.club.club.application.command.UpdateMemberProfileImageCommand;
import com.official.lockr.domain.club.club.application.usecase.AssignCoachUseCase;
import com.official.lockr.domain.club.club.application.usecase.AssignMangerUseCase;
import com.official.lockr.domain.club.club.application.usecase.ChangeJoinMethodUseCase;
import com.official.lockr.domain.club.club.application.usecase.ChangeMemberRoleUseCase;
import com.official.lockr.domain.club.club.application.usecase.ChangeVisibilityUseCase;
import com.official.lockr.domain.club.club.application.usecase.DelegatePresidentUseCase;
import com.official.lockr.domain.club.club.application.usecase.FoundClubUseCase;
import com.official.lockr.domain.club.club.application.usecase.LeaveClubUseCase;
import com.official.lockr.domain.club.club.application.usecase.RegisterClubMemberUseCase;
import com.official.lockr.domain.club.club.application.usecase.UpdateClubUseCase;
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
public class ClubService implements FoundClubUseCase, RegisterClubMemberUseCase, AssignMangerUseCase, AssignCoachUseCase, UpdateMemberProfileImageUseCase, DelegatePresidentUseCase, ChangeMemberRoleUseCase, ChangeVisibilityUseCase, ChangeJoinMethodUseCase, LeaveClubUseCase, UpdateClubUseCase {

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
        final String founderProfileImage = getFounderProfileImage(command.userId());
        final Club club = Club.init(
                command.userId(), command.name(), command.sportType(), command.city(),
                command.district(), command.description(), command.profileImageUrl(), command.backgroundImageUrl()
        );
        club.addMember(president(command.userId(), club.getId(), null, founderProfileImage));
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
        if (club.isPresidency(command.targetUserId()) || club.hasNotMember(command.targetUserId())) {
            throw new IllegalArgumentException();
        }
        club.assignCoach(command.targetUserId());
        return clubRepository.save(club);
    }

    @Override
    public Club assignManager(final AssignManagerCommand command) {
        final Club club = club(command.clubId(), command.userId());
        if (club.isPresidency(command.targetUserId()) || club.hasNotMember(command.targetUserId())) {
            throw new IllegalArgumentException();
        }
        club.assignManger(command.targetUserId());
        return clubRepository.save(club);
    }

    private Club club(final String clubId, final String userId) {
        final Club club = clubRepository.findById(clubId);
        if (isNull(club) || !club.isPresidency(userId)) {
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

    @Override
    public Club delegatePresident(final DelegatePresidentCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalStateException();
        }
        club.delegatePresident(command.userId(), command.targetUserId());
        return clubRepository.save(club);
    }

    @Override
    public Club changeMemberRole(final ChangeMemberRoleCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalStateException();
        }
        club.changeMemberRole(command.userId(), command.targetMemberId(), command.role());
        return clubRepository.save(club);
    }

    @Override
    public Club changeVisibility(final ChangeVisibilityCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalStateException();
        }
        club.changeVisibility(command.userId(), command.isPublic());
        return clubRepository.save(club);
    }

    @Override
    public Club changeJoinMethod(final ChangeJoinMethodCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalStateException();
        }
        club.changeJoinMethod(command.userId(), command.joinMethod());
        return clubRepository.save(club);
    }

    @Override
    public Club update(final UpdateClubCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalStateException();
        }
        club.updateInfo(command.userId(), command.name(), command.description(), command.city(), command.district(), command.profileImageUrl(), command.backgroundImageUrl());
        return clubRepository.save(club);
    }

    @Override
    public void leave(final LeaveClubCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalStateException();
        }
        club.removeMember(command.userId());
        clubRepository.save(club);
    }
}
