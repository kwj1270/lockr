package com.official.lockr.domain.team.team.infrastructure;

import com.official.lockr.domain.team.team.domain.Member;
import com.official.lockr.domain.team.team.domain.Team;
import com.official.lockr.domain.team.team.domain.TeamRepository;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.daos.TeamsDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.jooq.generated.tables.pojos.TeamsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Repository
public class JOOQTeamRepository implements TeamRepository {

    private final TeamsDao teamDao;
    private final MembersDao memberDao;

    public JOOQTeamRepository(final Configuration configuration) {
        this.teamDao = new TeamsDao(configuration);
        this.memberDao = new MembersDao(configuration);
    }

    @Override
    public Team findByName(final String name) {
        return null;
    }

    @Transactional
    @Override
    public Team save(final Team team) {
        final List<MembersEntity> membersEntities = team.getMembers()
                .stream()
                .map(this::entity)
                .toList();
        final TeamsEntity teamsEntity = entity(team);
        memberDao.insert(membersEntities);
        teamDao.insert(teamsEntity);
        return team;
    }

    private static TeamsEntity entity(final Team team) {
        return new TeamsEntity(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt(),
                team.getUpdatedAt(),
                team.getDeletedAt()
        );
    }

    private MembersEntity entity(final Member member) {
        final String memberRole = member.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        return new MembersEntity(
                member.getId(),
                member.getUserId(),
                memberRole,
                member.getTeamId(),
                member.getCreatedAt(),
                member.getUpdatedAt(),
                member.getDeletedAt()
        );
    }
}
