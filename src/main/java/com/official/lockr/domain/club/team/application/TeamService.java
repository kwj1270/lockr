package com.official.lockr.domain.club.team.application;

import com.official.lockr.domain.club.team.application.command.AddMemberCommand;
import com.official.lockr.domain.club.team.application.command.AssignManagerCommand;
import com.official.lockr.domain.club.team.application.command.FoundTeamCommand;
import com.official.lockr.domain.club.team.domain.Team;
import com.official.lockr.domain.club.team.domain.TeamRepository;
import com.official.lockr.global.util.UlidUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.official.lockr.domain.club.team.domain.Member.player;
import static com.official.lockr.domain.club.team.domain.Member.president;

@Service
public class TeamService implements FoundTeamUseCase, RegisterTeamMemberUseCase, AssignMangerUseCase {

    private final TeamRepository teamRepository;

    public TeamService(final TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Team found(final FoundTeamCommand command) {
        final Team existedTeam = teamRepository.findByName(command.name());
        if (Objects.nonNull(existedTeam)) {
            throw new IllegalStateException();
        }
        final Team team = new Team(UlidUtils.generateUlid(), command.name(), command.description());
        team.addMember(president(UlidUtils.generateUlid(), command.userId(), team.getId()));
        return teamRepository.save(team);
    }

    @Override
    public Team addMember(final AddMemberCommand command) {
        final Team team = teamRepository.findById(command.teamId());
        if (Objects.isNull(team)) {
            throw new IllegalStateException();
        }
        if (team.isExistedMember(command.userId())) {
            return team;
        }
        team.addMember(player(UlidUtils.generateUlid(), command.userId(), team.getId()));
        return teamRepository.save(team);
    }

    @Override
    public Team assignManager(final AssignManagerCommand command) {
        final Team team = teamRepository.findById(command.teamId());
        if (Objects.isNull(team)) {
            throw new IllegalArgumentException();
        }
        if (team.isNotPresident(command.userId()) || team.hasNotMember(command.targetMemberId())) {
            throw new IllegalArgumentException();
        }
        team.assignManger(command.targetMemberId());
        return teamRepository.save(team);
    }
}
