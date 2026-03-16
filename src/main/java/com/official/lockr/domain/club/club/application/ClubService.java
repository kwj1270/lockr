package com.official.lockr.domain.club.club.application;

import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.application.command.AssignManagerCommand;
import com.official.lockr.domain.club.club.application.command.FoundClubCommand;
import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.global.util.UlidUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.official.lockr.domain.club.club.domain.Member.player;
import static com.official.lockr.domain.club.club.domain.Member.president;

@Service
public class ClubService implements FoundClubUseCase, RegisterClubMemberUseCase, AssignMangerUseCase {

    private final ClubRepository clubRepository;

    public ClubService(final ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    @Override
    public Club found(final FoundClubCommand command) {
        final Club existedClub = clubRepository.findByName(command.name());
        if (Objects.nonNull(existedClub)) {
            throw new IllegalStateException();
        }
        final Club club = new Club(UlidUtils.generateUlid(), command.name(), command.description());
        club.addMember(president(UlidUtils.generateUlid(), command.userId(), club.getId()));
        return clubRepository.save(club);
    }

    @Override
    public Club addMember(final AddMemberCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (Objects.isNull(club)) {
            throw new IllegalStateException();
        }
        if (club.isExistedMember(command.userId())) {
            return club;
        }
        club.addMember(player(UlidUtils.generateUlid(), command.userId(), club.getId()));
        return clubRepository.save(club);
    }

    @Override
    public Club assignManager(final AssignManagerCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (Objects.isNull(club)) {
            throw new IllegalArgumentException();
        }
        if (club.isNotPresident(command.userId()) || club.hasNotMember(command.targetMemberId())) {
            throw new IllegalArgumentException();
        }
        club.assignManger(command.targetMemberId());
        return clubRepository.save(club);
    }
}
