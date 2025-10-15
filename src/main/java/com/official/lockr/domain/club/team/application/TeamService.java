package com.official.lockr.domain.club.team.application;

import com.github.f4b6a3.ulid.UlidCreator;
import com.official.lockr.domain.club.team.application.command.AddMemberCommand;
import com.official.lockr.domain.club.team.application.command.FoundTeamCommand;
import com.official.lockr.domain.club.team.domain.Team;
import com.official.lockr.domain.club.team.domain.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.official.lockr.domain.club.team.domain.Member.player;
import static com.official.lockr.domain.club.team.domain.Member.president;

@Service
public class TeamService implements FoundTeamUseCase, RegisterTeamMemberUseCase {

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
        final Team team = new Team(UlidCreator.getUlid().toString(), command.name(), command.description());
        team.addMember(president(UlidCreator.getUlid().toString(), command.userId(), team.getId()));
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
        team.addMember(player(UlidCreator.getUlid().toString(), command.userId(), team.getId()));
        return teamRepository.save(team);
    }
}
